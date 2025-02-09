package com.heima.wemedia.service;

import com.heima.model.wemedia.dtos.WmChannelPageReqDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 频道信息表 服务类
 * </p>
 *
 * @author author
 * @since 2024-10-11
 */
public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();

    ResponseResult deleteChannel(Integer id);

    ResponseResult listChannel(WmChannelPageReqDto dto);

    ResponseResult updateChannel(WmChannel wmChannel);

    ResponseResult saveChannel(WmChannel wmChannel);
}
