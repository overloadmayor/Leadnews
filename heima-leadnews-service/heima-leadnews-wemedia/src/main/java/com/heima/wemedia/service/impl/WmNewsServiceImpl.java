package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.NewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vo.WmNewsVo;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import org.apache.commons.beanutils.BeanUtils;
import com.heima.wemedia.service.WmNewsTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 自媒体图文内容信息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-10-11
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmUserMapper wmUserMapper;

    @Override
    public ResponseResult findlist(WmNewsPageReqDto dto) {
        dto.checkParam();
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus())
                .eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId())
                .between(dto.getBeginPubDate() != null && dto.getEndPubDate() != null,
                        WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate())
                .like(StringUtils.isNotBlank(dto.getKeyword()), WmNews::getTitle, dto.getKeyword())
                .orderByDesc(WmNews::getPublishTime);
        page = this.page(page, wrapper);
        //结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),
                dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;

    }

    @Override
    public ResponseResult submitNews(WmNewsDto dto) throws InvocationTargetException,
            IllegalAccessException, InterruptedException {
        if (dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //保存或修改文章
        WmNews wmNews = new WmNews();

        BeanUtils.copyProperties(dto, wmNews);
        System.out.println(dto.getImages());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            wmNews.setImages(StringUtils.join(dto.getImages(), ","));
        }
        System.out.println(dto);
        //如果当前封面类型为自动,-1
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        //判断是否为草稿,是草稿则结束当前关系
        if (WmNews.Status.NORMAL.getCode() == dto.getStatus()) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //不是草稿，保存文章内容图片与素材的关系
        //获取文章中的图片信息
        List<String> materials = actractUrlInfo(dto.getContent());
        saveRelativeInfoForContent(materials, wmNews.getId());

        //不是草稿，保存文章封面图片与素材的关系
        saveRelativeInfoForCover(dto, wmNews, materials);

        //审核文章
//        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        System.out.println("进入审核");
        wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        //检查参数
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //查询文章
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        //判断文章是否已经发布
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章不是发布状态，不能上下架");
        }

        //修改文章enable
        if (dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2) {
            update(new LambdaUpdateWrapper<WmNews>().set(WmNews::getEnable, dto.getEnable()).eq(WmNews::getId, dto.getId()));

            //发送消息，通知article修改文章配置
            if (wmNews.getArticleId() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("articleId", wmNews.getArticleId());
                map.put("enable", dto.getEnable());
                kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, JSON.toJSONString(map));
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult findScanList(NewsAuthDto dto) {
        dto.checkParam();
        dto.setPage((dto.getPage() - 1) * dto.getSize());
        List<WmNewsVo> listAndPage = wmNewsMapper.findListAndPage(dto);
        int listCount = wmNewsMapper.findListCount(dto);
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) listCount);
        pageResponseResult.setData(listAndPage);
        return pageResponseResult;
    }

    @Override
    public ResponseResult findScanNews(Integer id) {
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());

        WmNewsVo wmNewsVo = new WmNewsVo();
        BeanUtils.copyProperties(wmNews, wmNewsVo);
        if (wmUser != null) {
            wmNewsVo.setAuthorName(wmUser.getName());
        }
        return ResponseResult.okResult(wmNewsVo);
    }

    @Override
    public ResponseResult updateByStatus(Short status, NewsAuthDto dto) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        if(status==null||dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        wmNews.setStatus(status);
        if(StringUtils.isNotBlank(dto.getMsg())){
            wmNews.setReason(dto.getMsg());
        }
        updateById(wmNews);
        if(status.equals(WemediaConstants.WM_NEWS_AUTH_PASS)){
            ResponseResult responseResult = wmNewsAutoScanService.saveAppArticle(wmNews);
            if(responseResult.getCode()==AppHttpCodeEnum.SUCCESS.getCode()){
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
                wmNews.setArticleId((Long)responseResult.getData());
                updateById(wmNews);
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        //如果当前封面类型为自动，则设置封面类型的数据，
        //匹配规则：
        //1.如果内容图片大于等于1，小于3，单图 type 1
        //2.如果内容图片大于等于3 多图 type 3
        //3.无图 type 0
        //保存封面图片于素材的关系
        List<String> images = dto.getImages();
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            //多图
            if (materials.size() >= 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1) {
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            //修改文章
            if (images != null && !images.isEmpty()) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }
        if (images != null && !images.isEmpty()) {
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }

    }

    private void saveRelativeInfoForContent(List<String> materials, Integer newsId) {
        saveRelativeInfo(materials, newsId, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    /**
     * 保存文章图片与素材的关系到数据库中
     *
     * @param materials
     * @param type
     */
    private void saveRelativeInfo(List<String> materials, Integer newsId, Short type) {
        if (materials == null || materials.isEmpty()) {
            return;
        }
        //通过图片url查询素材的id
        List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(new LambdaQueryWrapper<WmMaterial>().in(WmMaterial::getUrl,
                materials));
        if (wmMaterials == null || wmMaterials.size() == 0 || materials.size() != wmMaterials.size()) {
            throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
        }


        List<Integer> idList = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());

        //批量保存
        wmNewsMaterialMapper.saveRelations(idList, newsId, type);
    }

    private List<String> actractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String url = map.get("value").toString();
                materials.add(url);
            }
        }
        return materials;
    }

    private void saveOrUpdateWmNews(WmNews wmNews) {

        //补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);
        if (wmNews.getId() == null) {
            this.save(wmNews);
        } else {
            wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>().eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }

    }
}
