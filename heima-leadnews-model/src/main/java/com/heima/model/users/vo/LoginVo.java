package com.heima.model.users.vo;

import lombok.Data;
import com.heima.model.users.pojos.ApUser;
@Data
public class LoginVo {
    private String token;
    private ApUser user;
}
