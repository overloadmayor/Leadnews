package com.heima.behavior.service;

import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ReadBehaviorService {

    ResponseResult readBehavior(ReadBehaviorDto dto);
}
