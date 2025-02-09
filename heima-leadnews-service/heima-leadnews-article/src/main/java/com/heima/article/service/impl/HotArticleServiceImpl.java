package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private IWemediaClient wemediaClient;
    @Autowired
    private CacheService cacheService;

    @Override
    public void computeHotArticle() {

        //查询前五天文章数据
        Date dateParam= DateTime.now().minusYears(5).toDate();
        List<ApArticle> apArticleList = apArticleMapper.findArticleListByLast5days(dateParam);

        //计算文章的分值
        List<HotArticleVo> hotArticleVoList=computeHotArticle(apArticleList);

        //为每个频道缓存30条分值较高的文章
        cacheTagToRedis(hotArticleVoList);
    }

    /**
     * 为每个频道缓存30条分值较高的文章
     * @param hotArticleVoList
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        ResponseResult responseResult = wemediaClient.getChannelList();
        if(responseResult.getCode() == 200){
            String jsonString = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(jsonString, WmChannel.class);
            if(hotArticleVoList!=null && hotArticleVoList.size()>0){
                for (WmChannel wmChannel : wmChannels) {
                    //给文章进行过滤和排序
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId()
                            .equals(wmChannel.getId())).sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                            .limit(30).collect(Collectors.toList());
                    cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE+wmChannel.getId(),JSON.toJSONString(hotArticleVos));

                }
            }
        }
        List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                .limit(30).collect(Collectors.toList());
        cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ArticleConstants.DEFAULT_TAG,
                JSON.toJSONString(hotArticleVos));

    }

    private List<HotArticleVo> computeHotArticle(List<ApArticle> apArticleList) {
        List<HotArticleVo> hotArticleVos = new ArrayList<>();
        if(apArticleList!=null||apArticleList.size()>0){
            for (ApArticle apArticle : apArticleList) {
                HotArticleVo hotArticleVo=new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hotArticleVo);
                Integer score=computeScore(apArticle);
                hotArticleVo.setScore(score);
                hotArticleVos.add(hotArticleVo);
            }
        }
        return hotArticleVos;
    }

    /**
     * 计算文章的具体分值
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score=0;
        if(apArticle.getLikes()!=null){
            score+=apArticle.getLikes()*ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if(apArticle.getViews()!=null){
            score+=apArticle.getViews();
        }
        if(apArticle.getComment()!=null){
            score+=apArticle.getComment()*ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if(apArticle.getCollection()!=null){
            score+=apArticle.getCollection()*ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;

    }
}
