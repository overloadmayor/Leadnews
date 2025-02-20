package com.heima.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HelloListener {
    @KafkaListener(topics = {"itcast-topic"})
    public void onMessage(String message) {
        if(!StringUtils.isEmpty(message))
            System.out.println(message);
    }
    @KafkaListener(topics = {"user-topic"})
    public void onMessage2(String message) {
        User user = JSON.parseObject(message, User.class);
        System.out.println(user.getUsername()+"  "+user.getAge());
    }

}
