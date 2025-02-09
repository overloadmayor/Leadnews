package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
public class WmSensitiveController {
    @Autowired
    private WmSensitiveService wmSensitiveService;

    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable Integer id) {
        return wmSensitiveService.delWords(id);
    }

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmSensitivePageReqDto dto) {
        return wmSensitiveService.listWords(dto);
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmSensitive sensitiveWord) {
        return wmSensitiveService.saveWords(sensitiveWord);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmSensitive sensitiveWord) {
        return wmSensitiveService.updateWords(sensitiveWord);
    }
}
