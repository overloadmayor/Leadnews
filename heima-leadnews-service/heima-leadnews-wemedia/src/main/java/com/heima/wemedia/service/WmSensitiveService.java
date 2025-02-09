package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {
    ResponseResult delWords(Integer id);

    ResponseResult listWords(WmSensitivePageReqDto dto);

    ResponseResult saveWords(WmSensitive sensitiveWord);

    ResponseResult updateWords(WmSensitive sensitiveWord);
}
