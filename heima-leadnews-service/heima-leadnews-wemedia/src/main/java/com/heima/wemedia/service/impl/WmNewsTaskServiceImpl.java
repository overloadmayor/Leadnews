package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

@Service
@Slf4j
@Async
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient scheduleClient;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Override
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中------------------- begin");

        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPrioriry());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        scheduleClient.addTask(task);

        log.info("添加任务到延迟服务中------------------- end");
    }

    @Scheduled(fixedRate = 1000)
    @Override
    public void scanNewsToTask() throws InterruptedException, InvocationTargetException, IllegalAccessException {

        ResponseResult responseResult=
                scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(),TaskTypeEnum.NEWS_SCAN_TIME.getPrioriry());

        if(responseResult.getCode().equals(200) && responseResult.getData()!=null){
            log.info("得到任务");
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(),WmNews.class);
            System.out.println("进入自动审核");
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }
}
