package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.behavior.service.ReadBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dto.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.users.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReadBehaviorServiceImpl implements ReadBehaviorService {
    @Autowired
    private CacheService cacheService;
//    @Autowired
//    private IArticleClient articleClient;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        //检查参数
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        String readJson =
                (String) cacheService.hGet(BehaviorConstants.READ_BEHAVIOR + dto.getArticleId(),
                user.getId().toString());
        if(readJson != null){
            ReadBehaviorDto readBehaviorDto = JSON.parseObject(readJson, ReadBehaviorDto.class);
            dto.setCount((short) (dto.getCount()+ readBehaviorDto.getCount()));
        }

        log.info("保存当前key:{}, {}, {}", dto.getArticleId(), user.getId(), dto);
        cacheService.hPut(BehaviorConstants.READ_BEHAVIOR + dto.getArticleId(),
                user.getId().toString(),JSON.toJSONString(dto));
        //发送消息，数据聚合
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(dto.getArticleId());
        mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
        mess.setAdd(1);
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC,JSON.toJSONString(mess));
//        ResponseResult responseResult = articleClient.updateArticleViews(dto);
//        if(responseResult.getCode() != AppHttpCodeEnum.SUCCESS.getCode()){
//            return responseResult;
//        }
        return ResponseResult.okResult(dto);
    }
}
