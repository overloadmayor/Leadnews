package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.users.dtos.LoginDto;
import com.heima.model.users.vo.LoginVo;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.model.users.pojos.ApUser;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Override
    public ResponseResult login(LoginDto loginDto) {
//        Map<String, Object> map = new HashMap<>();
        LoginVo loginVo = new LoginVo();
        if (StringUtils.isNotBlank(loginDto.getPassword()) && StringUtils.isNotBlank(loginDto.getPhone())) {
            ApUser apUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, loginDto.getPhone()));
            if (apUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "用户信息不存在");
            }
            //对比密码
            String password = loginDto.getPassword() + apUser.getSalt();
            String saltPswd = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!saltPswd.equals(apUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            //返回数据
            String token = AppJwtUtil.getToken(apUser.getId().longValue());
            apUser.setPassword(null);
            apUser.setSalt(null);

            loginVo.setUser(apUser);
            System.out.println("loginVo = " + loginVo);
            loginVo.setToken(token);
        } else {
            //游客登陆
            loginVo.setToken(AppJwtUtil.getToken(0L));
        }
        return ResponseResult.okResult(loginVo);
    }
}
