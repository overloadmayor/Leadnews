package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.behavior.dto.LikesBehaviorDto;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败");
    }

    @Override
    public ResponseResult updateArticleLikes(LikesBehaviorDto articleDto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"修改数据失败");

    }

    @Override
    public ResponseResult updateArticleViews(ReadBehaviorDto articleDto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"修改数据失败");

    }
}
