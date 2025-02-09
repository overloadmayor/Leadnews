package com.heima.user.service.impl;

import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.users.dtos.UserRelationDto;
import com.heima.model.users.pojos.ApUser;
import com.heima.user.service.UserRelationService;
import com.heima.utils.thread.AppThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Autowired
    private CacheService cacheService;

    @Override
    public ResponseResult follow(UserRelationDto dto) {
        if(dto==null||dto.getOperation()<0||dto.getOperation()>1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtil.getUser();
        System.out.println(user);
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN,"用户未登录");
        }
        Integer userId = user.getId();
        Integer authorId = dto.getAuthorId();
        if(dto.getOperation()==0){
            //关注
            cacheService.zAdd(BehaviorConstants.APUSER_FOLLOW_RELATION+userId,authorId.toString(),System.currentTimeMillis());
            cacheService.zAdd(BehaviorConstants.APUSER_FAN_RELATION+authorId,userId.toString(),
                    System.currentTimeMillis());
        }else{
            cacheService.zRemove(BehaviorConstants.APUSER_FOLLOW_RELATION+userId,authorId.toString());
            cacheService.zRemove(BehaviorConstants.APUSER_FAN_RELATION+authorId,userId.toString());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
