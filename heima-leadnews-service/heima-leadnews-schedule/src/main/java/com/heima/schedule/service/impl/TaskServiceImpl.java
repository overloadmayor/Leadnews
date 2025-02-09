package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.heima.common.redis.CacheService;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;
    @Override
    public long addTask(Task task) {
        //添加任务到数据库中
        boolean success=addTaskToDb(task);
        if(success){
            //添加任务到redis
            addTaskToCache(task);
        }

        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(long taskId) {
        boolean flag=false;
        //删除任务，更新任务日志
        Task task=updateDb(taskId,ScheduleConstants.CANCELLED);
        if(task!=null){
            removeTaskFromCache(task);
            flag=true;
        }
        return flag;
    }

    @Override
    public Task poll(int type, int priority) {
        Task task=null;
        try {

            String key=type+"_"+priority;

            //从redis中拉取数据
            String task_json=cacheService.lRightPop(ScheduleConstants.TOPIC+key);
            if(StringUtils.isNotBlank(task_json)){
                task=JSON.parseObject(task_json,Task.class);

                //修改数据库信息
                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("poll task exception:{}",e);
        }
        return task;
    }

    private void removeTaskFromCache(Task task) {
        String key=task.getTaskType()+"_"+task.getPriority();
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else{
            cacheService.zRemove(ScheduleConstants.FUTURE+key,JSON.toJSONString(task));
        }
    }


    /**
     * 删除任务，更新任务日志
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task=null;

        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);
            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task=new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.error("task cancel exception taskId={},reason={}",taskId,e);
        }
        return task;
    }

    /**
     * 把任务添加到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        String key=task.getTaskType()+"_"+task.getPriority();
        //获取五分钟后的毫秒值
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long timeInMillis = calendar.getTimeInMillis();

        //如果任务的执行时间小于等于当前时间，存入list
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC +key, JSON.toJSONString(task));
        }else if(task.getExecuteTime()<=timeInMillis){
            //如果任务的执行时间大于当前时间 && 小于预设时间（未来五分钟）存入zset中
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),task.getExecuteTime());
        }


    }

    /**
     * 添加任务到数据库中
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {
        boolean flag=false;
        try {
            //保存任务表
            Taskinfo taskinfo=new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //设置taskID
            task.setTaskId(taskinfo.getTaskId());

            //保存任务日志数据
            TaskinfoLogs taskinfoLogs=new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setVersion(1);//设置乐观锁版本号
            taskinfoLogs.setStatus(0);//初始化
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag=true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flag;
    }

    /**
     * 未来数据定时刷新
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void refresh()
    {
        String token=cacheService.tryLock("FUTURE_TASK_SYNC",1000*30);
        if(StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新--定时任务");
            //获取所有未来数据的集合key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

            for (String futureKey : futureKeys) {
                String topicKey=ScheduleConstants.TOPIC+futureKey.split(ScheduleConstants.FUTURE)[1];

                //按照key和分值查询符合条件的数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                //同步数据
                if(!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                    log.info("成功的将"+futureKey+"刷新到了"+topicKey);
                }
            }
        }
    }

    /**
     * 数据库任务定时同步到redis
     */
    @PostConstruct
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void reloadData()
    {
        //清除缓存中的数据 list zset
        clearCache();

        //查询符合条件的任务 小于未来5分钟的数据
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(new LambdaQueryWrapper<Taskinfo>().lt(Taskinfo::getExecuteTime,
                calendar.getTime()));
        //把任务添加到redis
        if(taskinfos!=null||taskinfos.size()>0){
            for(Taskinfo taskinfo:taskinfos){
                Task task=new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }

        log.info("数据库的任务同步到了redis");
    }

    private void clearCache() {
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }
}
