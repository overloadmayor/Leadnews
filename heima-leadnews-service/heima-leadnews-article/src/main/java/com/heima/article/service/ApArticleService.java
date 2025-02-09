package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.mess.ArticleVisitStreamMess;

import java.lang.reflect.InvocationTargetException;

public interface ApArticleService extends IService<ApArticle> {

    /**
     * 加载文章列表
     * @param articleHomeDto
     * @param type  1 加载更多 2 记载最新
     * @return
     */
    public ResponseResult load(ArticleHomeDto articleHomeDto,Short type);

    /**
     * 加载文章列表
     * @param articleHomeDto
     * @param type  1 加载更多 2 记载最新
     * @param firstPage true 是首页 false 不是首页
     * @return
     */
    public ResponseResult load2(ArticleHomeDto articleHomeDto,Short type,boolean firstPage);


    /**
     * 保存app相关文章
     * @param articleDto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto articleDto) throws InvocationTargetException, IllegalAccessException;

    /**
     * 加载文章详情，数据回显
     * @param dto
     * @return
     */
    ResponseResult loadArticleBehavior(ArticleInfoDto dto);

    /**
     * 更新文章的分值，同时更新缓存中的热点文章数据
     * @param mess
     */
    public void updateScore(ArticleVisitStreamMess mess);
}
