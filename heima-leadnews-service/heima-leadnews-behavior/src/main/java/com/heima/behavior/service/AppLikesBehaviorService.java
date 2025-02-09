package com.heima.behavior.service;

import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface AppLikesBehaviorService {
    /**
     * 点赞
     * @param dto
     */
    ResponseResult like(LikesBehaviorDto dto);
}
