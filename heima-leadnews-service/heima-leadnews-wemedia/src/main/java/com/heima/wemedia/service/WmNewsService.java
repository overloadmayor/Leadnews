package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsAuthDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>
 * 自媒体图文内容信息表 服务类
 * </p>
 *
 * @author author
 * @since 2024-10-11
 */
public interface WmNewsService extends IService<WmNews> {

    /**
     * 条件查询文章列表
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findlist(WmNewsPageReqDto wmNewsPageReqDto);

    public ResponseResult submitNews(WmNewsDto dto) throws InvocationTargetException,
            IllegalAccessException, InterruptedException;

    /**
     * 文章的上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto dto);

    /**
     * 查询审核文章列表
     * @param dto
     * @return
     */
    ResponseResult findScanList(NewsAuthDto dto);

    /**
     * 查看审核文章详情
     * @param id
     * @return
     */
    ResponseResult findScanNews(Integer id);

    /**
     * 更新审核状态
     * @param wmNewsAuthFail
     * @param dto
     * @return
     */
    ResponseResult updateByStatus(Short wmNewsAuthFail, NewsAuthDto dto) throws InterruptedException, InvocationTargetException, IllegalAccessException;
}
