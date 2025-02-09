package com.heima.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.heima.common.redis.CacheService;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private CacheService cacheService;

    @Test
    public void testRedis() {
        //在list的左边添加元素
        cacheService.lLeftPush("list_001","hello,redis");
        //在list的右边获取元素，并删除
        String list_001=cacheService.lLeftPop("list_001");
        System.out.println(list_001);
    }

    @Test
    public void testZset() {
        cacheService.zAdd("zset_key_001","hello zset 001",1000);
        cacheService.zAdd("zset_key_001","hello zset 002",8888);
        cacheService.zAdd("zset_key_001","hello zset 003",7777);
        cacheService.zAdd("zset_key_001","hello zset 004",99999);

        Set<String> zsetKey001 = cacheService.zRangeByScore("zset_key_001", 0, 8888);
        System.out.println(zsetKey001);
    }
}
