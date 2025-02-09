package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.users.pojos.ApUser;
import com.heima.model.users.dtos.LoginDto;

public interface ApUserService extends IService<ApUser> {

    /**
     * app端登陆功能
     * @param loginDto
     * @return
     */
    public ResponseResult login(LoginDto loginDto);
}
