package com.heima.wemedia.controller.v1;

import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult findlist(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findlist(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto) throws InvocationTargetException, IllegalAccessException, InterruptedException {
        return wmNewsService.submitNews(dto);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto){
        return wmNewsService.downOrUp(dto);
    }

    /**
     * 审核文章列表
     * @param dto
     * @return
     */
    @PostMapping("/list_vo")
    public ResponseResult listVo(@RequestBody NewsAuthDto dto){
        return wmNewsService.findScanList(dto);
    }

    @GetMapping("/one_vo/{id}")
    public ResponseResult findWmNewsVo(@PathVariable Integer id){
        return wmNewsService.findScanNews(id);
    }

    /**
     * 审核失败
     * @param dto
     * @return
     */
    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto dto) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        return wmNewsService.updateByStatus(WemediaConstants.WM_NEWS_AUTH_FAIL, dto);
    }

    /**
     * 审核成功
     * @param dto
     * @return
     */
    @PostMapping("/auth_pass")
    public ResponseResult authpass(@RequestBody NewsAuthDto dto) throws InterruptedException,
            InvocationTargetException, IllegalAccessException {
        return wmNewsService.updateByStatus(WemediaConstants.WM_NEWS_AUTH_PASS, dto);
    }
}
