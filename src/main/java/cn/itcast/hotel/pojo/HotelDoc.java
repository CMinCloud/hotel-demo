package cn.itcast.hotel.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    //    排序时的距离
    private Object distance;
//    isAD设置广告标签并指定
    private Boolean isAD;
//    添加Suggestion存储 需要自动补充的字段  ,要与索引设置一致才行，否则传输的对象被解析为json不同则找不到值
    private List<String> suggestion;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();

        if(this.business.contains("/")){
//            如果包含“/”说明属于多个商圈
            String[] splits = this.business.split("/");
            this.suggestion = new ArrayList<>();
//            将business和brand封装进suggestions
            suggestion.add(this.brand);
            // 泛型方法，参数一填写需要存储的参数类型的父类，参数二是需要存储的参数
            Collections.addAll(this.suggestion,splits);
        }else if(this.business.contains("、")){
            //            如果包含“/”说明属于多个商圈
            String[] splits = this.business.split("、");
            this.suggestion = new ArrayList<>();
//            将business和brand封装进suggestions
            suggestion.add(this.brand);
            // 泛型方法，参数一填写需要存储的参数类型的父类，参数二是需要存储的参数
            Collections.addAll(this.suggestion,splits);
        }else {
            this.suggestion = Arrays.asList(this.business,this.brand);
        }
    }
}
