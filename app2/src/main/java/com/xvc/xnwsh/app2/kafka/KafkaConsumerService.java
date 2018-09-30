package com.xvc.xnwsh.app2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created  on 18-9-28.
 */
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public static void main(String[] args) {
        System.out.println("111");
       // KafkaConsumerService.CommonDemo();
    }

    public static void CommonDemo() {
        final Properties properties = new Properties() {{
            put("bootstrap.servers", "crrc1:9092");
            put("group.id", "testAPIdemo");
            put("enable.auto.commit", "true");
            put("auto.commit.interval.ms", "5000");
            put("session.timeout.ms", "30000");
            put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            put("auto.offset.reset","latest");
        }};
        //自动提交，会有问题
        //1.默认会5秒提交一次offset，但是中间停止的话会造成重复消费
        //2.新添加进消费者组的时候，会再均衡，默认从上次消费提交的地方开始，消息重复
        //3.自动提交，虽然提交了偏移量，但并不知道，哪些消息被处理了，是否处理成功，偏移量是否提交成功
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList("testAPI"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                record.toString();
                log.info("Consumer  "+record.topic()+"   "+record.partition()+"   "+record.offset()+"  "+record.value());
            }
        }
    }

}
