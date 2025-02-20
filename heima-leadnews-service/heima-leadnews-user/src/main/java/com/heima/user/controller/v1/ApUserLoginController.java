package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.users.dtos.LoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
@ApiOperation(value = "app端用户登陆",tags="app端用户登陆")
public class ApUserLoginController {
    private final ApUserService apUserService;

    @PostMapping("login_auth")
    @ApiOperation("用户登陆")
    public ResponseResult login(@RequestBody LoginDto loginDto){
        return apUserService.login(loginDto);
    }
}
