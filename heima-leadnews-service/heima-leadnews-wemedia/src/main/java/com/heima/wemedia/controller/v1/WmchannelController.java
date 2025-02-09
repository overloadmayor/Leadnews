package com.heima.wemedia.controller.v1;

import com.heima.model.wemedia.dtos.WmChannelPageReqDto;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
@RequiredArgsConstructor
public class WmchannelController {
    private final WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult channels() {
        return wmChannelService.findAll();
    }

    @GetMapping("/del/{id}")
    public ResponseResult del(@PathVariable Integer id) {
        return wmChannelService.deleteChannel(id);
    }
    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmChannelPageReqDto dto){
        return wmChannelService.listChannel(dto);
    }
    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel adChannel)
    {
        return wmChannelService.saveChannel(adChannel);
    }
    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmChannel adChannel)
    {
        return wmChannelService.updateChannel(adChannel);
    }
}
