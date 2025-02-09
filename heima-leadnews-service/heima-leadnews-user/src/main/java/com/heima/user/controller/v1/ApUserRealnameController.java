package com.heima.user.controller.v1;

import com.heima.common.constants.UserConstants;
import com.heima.model.common.dtos.ResponseResult;

import com.heima.model.users.dtos.AuthDto;
import com.heima.user.service.ApUserRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ApUserRealnameController {
    @Autowired
    private ApUserRealnameService apUserRealnameService;

    /**
     * 查询用户列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody AuthDto dto) {
        return apUserRealnameService.list(dto);
    }
    /**
     * 审核不通过
     * @param dto
     * @return
     */
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return apUserRealnameService.updateByStatus(dto, UserConstants.FAIL_AUTH);
    }

    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return apUserRealnameService.updateByStatus(dto, UserConstants.PASS_AUTH);
    }
}