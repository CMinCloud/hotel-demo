package cn.itcast.hotel.constants;


public class MqConstants {

//    交换机名称，使用topic连接模式
    public static final String HOTEL_EXCHANGE = "hotel.topic";

//    监听新转增和修改的队列
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";

//    监听删除的队列
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";

//    新增或修改的RoutingKey
    public static final String HOTEL_INSERT_KET = "hotel.insert";

//    删除的RoutingKey
    public static final String HOTEL_DELETE_KEY = "hotel.delete";

}
