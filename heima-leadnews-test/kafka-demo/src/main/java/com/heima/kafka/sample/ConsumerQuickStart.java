package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * 消费者
 */
public class ConsumerQuickStart {
    public static void main(String[] args) {
        //kafka连接配置信息
        Properties props = new Properties();
        //kafka连接地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        //key和value的反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common" +
                ".serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common" +
                ".serialization.StringDeserializer");
        //设置消费者组
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");
        //手动提交偏移量
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        //创建消费之对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //订阅主题
        consumer.subscribe(Collections.singletonList("itcast-topic-out"));

        //同步和异步提交偏移量
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords =
                        consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : consumerRecords) {
                    System.out.println(record.key());
                    System.out.println(record.value());
                    System.out.println(record.offset());
                    System.out.println(record.partition());
                }
                //异步提交偏移量
                consumer.commitAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("记录错误的信息"+e);
        }finally {
            //同步
            consumer.commitSync();
        }

        //拉取消息
//        while (true) {
//            ConsumerRecords<String, String> consumerRecords =
//                    consumer.poll(Duration.ofMillis(1000));
//            for (ConsumerRecord<String, String> record : consumerRecords) {
//                System.out.println(record.key());
//                System.out.println(record.value());
//                System.out.println(record.offset());
//                System.out.println(record.partition()) ;
//
////                try{
////                    //同步提交偏移量
////                    consumer.commitSync();
////                }catch (CommitFailedException e){
////                    System.out.println("记录提交失败的异常:"+e);
////                }
//
////                //异步的方式提交偏移量
////                consumer.commitAsync(new OffsetCommitCallback() {
////                    @Override
////                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
////                        if(exception!=null){
////                            System.out.println("记录错误的提交偏移量"+offsets+",异常信息为:"+exception.getMessage());
////                        }
////                    }
////                });
//
//            }
//        }

    }
}
