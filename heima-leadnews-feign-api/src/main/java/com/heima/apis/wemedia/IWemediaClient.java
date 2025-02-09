package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-wemedia")
public interface IWemediaClient {
    @GetMapping("/api/v1/user/{name}")
    public WmUser findByName(@PathVariable("name") String name);

    @PostMapping("/api/v1/save")
    public void save(@RequestBody WmUser Wmuser);

    @GetMapping("/api/v1/channel/list")
    public ResponseResult getChannelList();
}
