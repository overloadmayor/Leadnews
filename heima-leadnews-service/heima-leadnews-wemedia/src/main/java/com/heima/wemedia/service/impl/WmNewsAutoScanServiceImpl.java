package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.heima.apis.article.IArticleClient;
import com.heima.common.test4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    @Autowired
    private Tess4jClient tess4jClient;
    /**
     * 自媒体文章审核
     *
     * @param id
     */
    @Override
    @Async //表明当前方法是一个异步方法
    @GlobalTransactional
    public void autoScanWmNews(Integer id) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        //查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode()))//文章处于待审核状态
        {
            //提取文章内容和封面图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            //自管理的敏感词过滤
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if (!isSensitive) {
                return ;
            }

            //审核文本内容 阿里云接口
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);
            if (!isTextScan) {
                return;
            }

            //审核图片，阿里云接口
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isTextScan) {
                return;
            }

//            //审核成功，保存app端的相关的文章数据
//            ResponseResult responseResult = saveAppArticle(wmNews);
//            if(!responseResult.getCode().equals(200)){
//                throw new RuntimeException("wmNewsAutoScanServiceImpl-文章审核，保存相关文章数据失败");
//            }
//            //回填article_id
//            wmNews.setArticleId((Long)responseResult.getData());
            log.info("更新审核状态");
            updateWmNews(wmNews, (short) 3,"审核成功");

        }
    }

    /**
     * 自管理的敏感词审核
     * @param content
     * @param wmNews
     * @return
     */
    @Transactional
    public boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag=true;

        //获取所有的敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(new LambdaQueryWrapper<WmSensitive>().select(WmSensitive::getSensitives));
        List<String> sensitiveList=wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);
        //查看文章中是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0){
            updateWmNews(wmNews,(short)2,"当前文章中存在违规内容"+map);
            flag=false;
        }
        return flag;
    }

    @Override
    @GlobalTransactional
    public ResponseResult saveAppArticle(WmNews wmNews) throws InterruptedException,
            InvocationTargetException, IllegalAccessException {
        ArticleDto dto = new ArticleDto();
        //属性的拷贝
        BeanUtils.copyProperties(wmNews, dto);
        //文章的布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null)
            dto.setChannelName(wmChannel.getName());

        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null)
            dto.setAuthorName(wmUser.getName());

        if (wmNews.getArticleId() != null)
            dto.setId(wmNews.getArticleId());

        dto.setCreatedTime(new Date());
        return articleClient.saveArticle(dto);
    }

    /**
     * 审核图片
     *
     * @param images
     * @param wmNews
     * @return
     */
    @Transactional
    public boolean handleImageScan(List<String> images, WmNews wmNews)  {
        //下载图片 minIO
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();
        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                //byte[]转换为bufferedImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage= ImageIO.read(in);

                //图片识别
                String result=tess4jClient.doOCR(bufferedImage);
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if (!isSensitive) {
                    return isSensitive;
                }
                imageList.add(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean flag = true;
        return flag;
    }

    /**
     * 审核纯文本
     *
     * @param content
     * @param wmNews
     * @return
     */
    @Transactional
    public boolean handleTextScan(String content, WmNews wmNews) {
//        Map map=greenTextScan.greeTextScan(content);
        boolean flag = true;
        if(flag)return flag;
        if (content.length() == 0) {
            return flag;
        }

        try {
            Map map = new HashMap();
            String sug = "suggestion";
            map.put(sug, "pass");

            if (map != null && !map.isEmpty()) {
                if (map.get(sug).equals("block")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "文章存在违规内容");
                } else if (map.get(sug).equals("review")) {
                    flag = false;
                    updateWmNews(wmNews, (short) 2, "文章存在不确定内容");
                }
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    @Transactional
    public void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储文本内容
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                } else if (map.get("type").equals("image")) {
                    images.add(map.get("value").toString());
                }
            }
        }
        //提取文章的封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);
        return resultMap;
    }
}
