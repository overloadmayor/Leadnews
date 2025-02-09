package com.heima.kafka.sample;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * 生产者
 */
public class ProducerQuickStart {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //kafka连接配置信息
        Properties props = new Properties();
        //kafka连接地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        //key和value的序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common" +
                ".serialization.StringSerializer");
        //ack配置，消息确认机制
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        //重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, 10);
        //数据压缩
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        //创建kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        for(int i=0;i<5;i++){
            ProducerRecord<String,String>kvProducerRecord=new ProducerRecord<>("itcast-topic" +
                    "-input","hello kafka");
            producer.send(kvProducerRecord);
        }
//        //发送消息
//        ProducerRecord<String,String> kvProducerRecord=new ProducerRecord<String,String>("topic" +
//                "-first","key-001","hello,kafka");
//        同步
//        RecordMetadata recordMetadata = producer.send(kvProducerRecord).get();
//        System.out.println(recordMetadata.offset());
//        异步
//        producer.send(kvProducerRecord,new Callback() {
//            @Override
//            public void onCompletion(RecordMetadata metadata, Exception exception) {
//                if(exception!=null){
//                    System.out.println("记录日常信息到日志表中");
//                }
//                System.out.println(metadata.offset());
//            }
//        });
        //关闭消息通道,否则消息发送不成功
        producer.close();
    }
}
