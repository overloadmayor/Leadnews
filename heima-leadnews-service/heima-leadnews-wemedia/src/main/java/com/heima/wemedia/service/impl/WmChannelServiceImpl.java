package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.wemedia.dtos.WmChannelPageReqDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 频道信息表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-10-11
 */
@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {

    @Autowired
    private WmNewsService wmNewsService;

    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    @Override
    public ResponseResult deleteChannel(Integer id) {
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmChannel wmChannel = getById(id);
        if(wmChannel==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if(wmChannel.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道有效，不能删除");
        }
        int count = wmNewsService.count(new LambdaQueryWrapper<WmNews>()
                .eq(WmNews::getChannelId, id)
                .eq(WmNews::getStatus, WmNews.Status.PUBLISHED));
        if(count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"该频道有文章引用");
        }
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult listChannel(WmChannelPageReqDto dto) {
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        IPage<WmChannel> page=new Page<>(dto.getPage(),dto.getSize());
        page = page(page, new LambdaQueryWrapper<WmChannel>()
                .like(WmChannel::getName, "%" + dto.getName() + "%"));

        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(),
                dto.getSize(),
                (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    @Override
    public ResponseResult updateChannel(WmChannel wmChanneldto) {
        WmChannel wmChannel = getById(wmChanneldto.getId());
        if(wmChannel==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        int count = wmNewsService.count(new LambdaQueryWrapper<WmNews>()
                .eq(WmNews::getChannelId, wmChanneldto.getId())
                .eq(WmNews::getStatus, WmNews.Status.PUBLISHED));
        if(count>0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"该频道有文章引用");
        }
        updateById(wmChanneldto);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult saveChannel(WmChannel dto) {
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmChannel wmChannel = getOne(new LambdaQueryWrapper<WmChannel>()
                .eq(WmChannel::getName, dto.getName())
        );
        if(wmChannel!=null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"频道已存在");
        }
        dto.setCreatedTime(new Date());
        save(dto);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
