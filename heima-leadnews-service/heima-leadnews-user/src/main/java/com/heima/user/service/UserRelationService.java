package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.users.dtos.UserRelationDto;

public interface UserRelationService {
    //关注与取消关注
    ResponseResult follow(UserRelationDto dto);
}
