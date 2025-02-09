package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.users.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    private final ApArticleMapper articleMapper;
    private final ApArticleConfigMapper apArticleConfigMapper;
    private final ApArticleContentMapper apArticleContentMapper;
    private final ArticleFreemarkerService articleFreemarkerService;
    private final CacheService cacheService;

    @Override
    public ResponseResult load(ArticleHomeDto articleHomeDto, Short type) {
        //检验参数
        //分页参数的校验
        Integer size = articleHomeDto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        size = Math.min(size, ArticleConstants.MAX_PAGE_SIZE);
        if (!ArticleConstants.LOADTYPE_LOAD_MORE.equals(type) && !ArticleConstants.LOADTYPE_LOAD_NEW.equals(type)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //频道参数校验
        if (StringUtils.isBlank(articleHomeDto.getTag())) {
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        if (articleHomeDto.getMaxBehotTime() == null) articleHomeDto.setMaxBehotTime(new Date());
        if (articleHomeDto.getMinBehotTime() == null) articleHomeDto.setMinBehotTime(new Date());

        List<ApArticle> apArticles = articleMapper.loadArticleList(articleHomeDto, type);
        return ResponseResult.okResult(apArticles);
    }

    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage) {
        if (firstPage) {
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if (StringUtils.isNotBlank(jsonStr)) {
                List<HotArticleVo> hotArticleVos = JSON.parseArray(jsonStr, HotArticleVo.class);
                ResponseResult responseResult = ResponseResult.okResult(hotArticleVos);
                return responseResult;
            }
        }
        return load(dto, type);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public ResponseResult saveArticle(ArticleDto dto) throws InvocationTargetException, IllegalAccessException {
        //检查参数
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        if (dto.getId() == null) {
            //保存文章
            save(apArticle);

            //保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            //保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        } else {
            //修改文章
            updateById(apArticle);
            //修改文章内容

            LambdaUpdateWrapper<ApArticleContent> updateWrapper = Wrappers.<ApArticleContent>lambdaUpdate()
                    .set(ApArticleContent::getContent, dto.getContent())
                    .eq(ApArticleContent::getArticleId, apArticle.getId());
            apArticleContentMapper.update(null, updateWrapper);
        }
        //异步调用，生成静态文件上传到minio中

        articleFreemarkerService.buildArticleFreemarker(apArticle, dto.getContent());

        return ResponseResult.okResult(apArticle.getId());
    }

    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        if (dto == null || dto.getArticleId() == null || dto.getAuthorId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        boolean isfollow = false, islike = false, isunlike = false, iscollection = false;
        ApUser user = AppThreadLocalUtil.getUser();
        if (user != null) {
            //喜欢行为
            String likeBehaviorJson =
                    (String) cacheService.hGet(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            if (StringUtils.isNotBlank(likeBehaviorJson)) {
                islike = true;
            }
            //不喜欢的行为
            String unLikeBehaviorJson =
                    (String) cacheService.hGet(BehaviorConstants.UN_LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            if (StringUtils.isNotBlank(unLikeBehaviorJson)) {
                isunlike = true;
            }
            //是否收藏
            String collctionJson = (String) cacheService.hGet(BehaviorConstants.COLLECTION_BEHAVIOR + user.getId(), dto.getArticleId().toString());
            if (StringUtils.isNotBlank(collctionJson)) {
                iscollection = true;
            }
            //是否关注
            Double score = cacheService.zScore(BehaviorConstants.APUSER_FOLLOW_RELATION + user.getId(), dto.getAuthorId().toString());
            System.out.println(score);
            if (score != null) {
                isfollow = true;
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("isfollow", isfollow);
        resultMap.put("islike", islike);
        resultMap.put("isunlike", isunlike);
        resultMap.put("iscollection", iscollection);

        return ResponseResult.okResult(resultMap);
    }

    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        log.info("开始排序");
        //更新文章的阅读，点赞，收藏，评论的数量
        ApArticle apArticle = updateArticle(mess);
        //计算文章的分值
        Integer score = computeScore(apArticle);
        score = score * 3;
        replaceDataToRedis(apArticle, score,ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());
        //替换推荐对应的热点数据
        replaceDataToRedis(apArticle,score,ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 替换数据并且存入到redis
     * @param apArticle
     * @param score
     * @param topic
     */

    private void replaceDataToRedis(ApArticle apArticle, Integer score,String topic) {
        String articleListStr = cacheService.get(topic);
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVos = JSON.parseArray(articleListStr, HotArticleVo.class);
            boolean flag = true;
            //缓存中存在该文章，只更新分值
            for (HotArticleVo hotArticleVo : hotArticleVos) {
                if (hotArticleVo!=null&&hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }

            //如果缓存中不存在，查询缓存中分值最小的一条数据
            if (flag && hotArticleVos.size() >= 30) {
                hotArticleVos =
                        hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                HotArticleVo hotArticleVo = hotArticleVos.get(hotArticleVos.size() - 1);
                if(hotArticleVo.getScore()< score){
                    hotArticleVos.remove(hotArticleVos.size()-1);
                    HotArticleVo hot = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hot);
                    hot.setScore(score);
                    hotArticleVos.add(hot);
                }


            } else if(flag){
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hotArticleVo);
                hotArticleVo.setScore(score);
                hotArticleVos.add(hotArticleVo);
            }
            //缓存到redis
            hotArticleVos =
                    hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());

            cacheService.set(topic, JSON.toJSONString(hotArticleVos));

        }
    }

    /**
     * 更新文章行为数量
     *
     * @param mess
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection() + mess.getCollect());
        apArticle.setComment(apArticle.getComment() + mess.getComment());
        apArticle.setLikes(apArticle.getLikes() + mess.getLike());
        apArticle.setViews(apArticle.getViews() + mess.getView());
        updateById(apArticle);
        return apArticle;
    }

    /**
     * 计算文章的具体分值
     *
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getLikes() != null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null) {
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null) {
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;

    }
}
