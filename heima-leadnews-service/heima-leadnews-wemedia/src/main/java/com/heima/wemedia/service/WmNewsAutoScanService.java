package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmNews;

import java.lang.reflect.InvocationTargetException;

public interface WmNewsAutoScanService {

    /**
     * 自媒体文章审核
     * @param id
     */
    public void autoScanWmNews(Integer id) throws InterruptedException, InvocationTargetException, IllegalAccessException;

    /**
     * 保存app端文章
     * @param wmNews
     * @return
     */
    public ResponseResult saveAppArticle(WmNews wmNews)throws InterruptedException,InvocationTargetException, IllegalAccessException;
}
