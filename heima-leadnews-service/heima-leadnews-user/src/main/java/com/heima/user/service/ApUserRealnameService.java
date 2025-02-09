package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.users.dtos.AuthDto;
import com.heima.model.users.pojos.ApUserRealname;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    ResponseResult list(AuthDto dto);

    ResponseResult updateByStatus(AuthDto dto, Short failAuth);
}
