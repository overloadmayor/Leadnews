package com.heima.article.feign;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

@RestController
public class AricleClient implements IArticleClient {
    @Autowired
    private ApArticleService apArticleService;

    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        return apArticleService.saveArticle(articleDto);
    }

    @Override
    @PostMapping(value = "/api/v1/article/update/likes")
    public ResponseResult updateArticleLikes(@RequestBody LikesBehaviorDto dto)
    {

        if(dto.getOperation()==0){
            apArticleService.update(
                    new LambdaUpdateWrapper<ApArticle>()
                            .setSql("likes=likes+1")
                            .eq(ApArticle::getId,dto.getArticleId())
            );
        }else{
            apArticleService.update(
                    new LambdaUpdateWrapper<ApArticle>()
                            .setSql("likes=likes-1")
                            .eq(ApArticle::getId,dto.getArticleId())
            );
        };
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @PostMapping(value = "/api/v1/article/update/views")
    public ResponseResult updateArticleViews(@RequestBody ReadBehaviorDto dto){
        System.out.println("阅读");
        apArticleService.update(
                new LambdaUpdateWrapper<ApArticle>()
                        .set(ApArticle::getViews,dto.getCount())
                        .eq(ApArticle::getId,dto.getArticleId())
        );
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


}
