package com.heima.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloJob {
    @Value("${server.port}")
    private String port;

    @XxlJob("demoJobHandler")
    public void HelloJob(){
        System.out.println("简单任务执行了1"+port);
    }

    @XxlJob("shardingJobHandler")
    public void shardingJob(){
        //分片的参数
        int shardIndex= XxlJobHelper.getShardIndex();
        int shardTotal= XxlJobHelper.getShardTotal();
        System.out.println("shardIndex:"+shardIndex);
        System.out.println("shardTotal:"+shardTotal);
        //业务逻辑
        List<Integer> list = getList();
        for (Integer i : list) {
            if(i%shardTotal==shardIndex){
                System.out.println("当前第"+shardIndex+"分片执行了，任务项为："+i);
            }
        }
    }

    public List<Integer> getList(){
        ArrayList<Integer> list = new ArrayList<>();
        for(int i=0; i<10000; i++){
            list.add(i);
        }
        return list;
    }
}
