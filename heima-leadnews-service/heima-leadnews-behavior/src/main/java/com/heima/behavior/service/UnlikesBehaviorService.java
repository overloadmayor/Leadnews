package com.heima.behavior.service;

import com.heima.model.behavior.dto.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface UnlikesBehaviorService {
    ResponseResult unLike(UnLikesBehaviorDto dto);
}
