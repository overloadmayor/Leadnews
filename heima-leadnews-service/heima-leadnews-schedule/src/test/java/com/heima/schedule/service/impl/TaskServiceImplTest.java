package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private CacheService cacheService;

    @Test
    public void addTask() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task();
            task.setTaskType(100);
            task.setPriority(50);
            task.setParameters("task test".getBytes());
            task.setExecuteTime(new Date().getTime() + 5000);

            long taskId = taskService.addTask(task);
        }
//        System.out.println(taskId);
    }

    @Test
    public void cancelTask() {
        taskService.cancelTask(1846744855986008065L);
    }

    @Test
    public void testPoll() {
        Task task = taskService.poll(100, 50);
        System.out.println(task);
    }

    @Test
    public void testKeys() {
        Set<String> keys = cacheService.keys("future_*");
        System.out.println(keys);
        Set<String> keys1 = cacheService.scan("future_*");
        System.out.println(keys1);
    }

    //耗时6151
    @Test
    public void testPiple1() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时" + (System.currentTimeMillis() - start));
    }


    @Test
    public void testPiple2() {
        long start = System.currentTimeMillis();
        //使用管道技术
        List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (int i = 0; i < 10000; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    redisConnection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:" + (System.currentTimeMillis() - start) + "毫秒");
    }



}