package cn.itcast.hotel.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static cn.itcast.hotel.constants.MqConstants.*;

@Component
public class MqConfig {

//    需要使用Bean加载

    //    声明交换机
    @Bean
    public TopicExchange topicExchange() {
//        durable 默认 true， autoDelete默认false
        return new TopicExchange(HOTEL_EXCHANGE, true, false);
    }

    //    声明新增或修改队列
    @Bean
    public Queue insertQueue() {
        return new Queue(HOTEL_INSERT_QUEUE);
    }

    //    声明删除队列
    @Bean
    public Queue deleteQueue() {
        return new Queue(HOTEL_DELETE_QUEUE);
    }

    //    将队列绑定到交换机上，使用topic连接方式，设置RoutingKey
    @Bean                                   // 使用自动装配填入形参
    public Binding BindingInsesrtQueue(Queue insertQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(insertQueue).to(topicExchange).with(HOTEL_INSERT_KET);
    }

    @Bean
    public Binding BindingDeleteQueue(Queue deleteQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(deleteQueue).to(topicExchange).with(HOTEL_DELETE_KEY);
    }
}
