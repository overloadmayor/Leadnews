package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.apache.commons.net.nntp.Article;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.InvocationTargetException;

@FeignClient(value="leadnews-article",fallback= IArticleClientFallback.class)
public interface IArticleClient {

    @PostMapping(value="/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto) throws InterruptedException, InvocationTargetException, IllegalAccessException;

    @PostMapping(value = "/api/v1/article/update/likes")
    public ResponseResult updateArticleLikes(@RequestBody LikesBehaviorDto articleDto) ;


    @PostMapping(value = "/api/v1/article/update/views")
    public ResponseResult updateArticleViews(@RequestBody ReadBehaviorDto articleDto) ;

}
