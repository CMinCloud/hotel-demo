package cn.itcast.hotel.pojo.params;

import lombok.Data;

@Data
public class RequestParam {
    private String key;   //搜索关键字
    private Integer page;
    private Integer size;
    private String sortBy;  //排序规则

//    过滤条件
    private String brand;  //酒店品牌
    private String city;
    private String starName;  //酒店星级
    private Integer maxPrice;
    private Integer minPrice;
//    当前地理坐标
    private String location;

}
