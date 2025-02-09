package com.heima.wemedia.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WemediaClient implements IWemediaClient {
    @Autowired
    private WmUserService wmUserService;

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/api/v1/user/{name}")
    @Override
    public WmUser findByName(@PathVariable("name") String name){
        System.out.println("findByName:"+name);
        return wmUserService.getOne(new LambdaQueryWrapper<WmUser>().eq(WmUser::getName, name));
    }

    @PostMapping("/api/v1/save")
    @Override
    public void save(@RequestBody WmUser Wmuser){
        System.out.println("save wmuser");
        wmUserService.save(Wmuser);
    }

    @GetMapping("/api/v1/channel/list")
    @Override
    public ResponseResult getChannelList() {
        return wmChannelService.findAll();
    }
}
