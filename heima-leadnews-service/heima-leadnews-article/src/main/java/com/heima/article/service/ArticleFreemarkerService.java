package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;
import org.apache.commons.net.nntp.Article;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;

@Transactional
public interface ArticleFreemarkerService {
    /**
     * 生成静态文件上传到minIO中
     * @param apArticle
     * @param content
     */
    public void buildArticleFreemarker(ApArticle apArticle,String content) throws InvocationTargetException, IllegalAccessException;
}
