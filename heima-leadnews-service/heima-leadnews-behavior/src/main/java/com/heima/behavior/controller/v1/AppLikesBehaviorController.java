package com.heima.behavior.controller.v1;

import com.heima.behavior.service.AppLikesBehaviorService;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/likes_behavior")
@RestController
public class AppLikesBehaviorController{

    @Autowired
    private AppLikesBehaviorService appLikesBehaviorService;

    @PostMapping
    public ResponseResult like(@RequestBody LikesBehaviorDto dto){
       return appLikesBehaviorService.like(dto);
    }
}
