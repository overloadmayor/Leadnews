package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ApCollectionService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.users.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApCollectionServiceImpl implements ApCollectionService {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private ApArticleService apArticleService;

    /**
     * 收藏文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult collection(CollectionBehaviorDto dto) {

        //条件判断
        if (dto == null || dto.getEntryId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //判断是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //查询
        String collectionJson =
                (String) cacheService.hGet(BehaviorConstants.COLLECTION_BEHAVIOR + user.getId(),
                        dto.getEntryId().toString());
        if (StringUtils.isNotBlank(collectionJson) && dto.getOperation() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章已收藏");
        }

        //收藏
        if (dto.getOperation() == 0) {
            log.info("文章收藏，保存key:{},{},{}",dto.getEntryId(),user.getId().toString(), JSON.toJSONString(dto));
            cacheService.hPut(BehaviorConstants.COLLECTION_BEHAVIOR + user.getId(), dto.getEntryId().toString(), JSON.toJSONString(dto));
            apArticleService.update(
                    new LambdaUpdateWrapper<ApArticle>()
                            .setSql("collection=collection+1")
                            .eq(ApArticle::getId, dto.getEntryId())
            );
        } else {
            //取消收藏
            log.info("文章收藏，删除key:{},{},{}",dto.getEntryId(),user.getId().toString(), JSON.toJSONString(dto));
            cacheService.hDelete(BehaviorConstants.COLLECTION_BEHAVIOR + user.getId(), dto.getEntryId().toString());
            apArticleService.update(
                    new LambdaUpdateWrapper<ApArticle>()
                            .setSql("collection=collection-1")
                            .eq(ApArticle::getId, dto.getEntryId())
            );
        }


        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

}
