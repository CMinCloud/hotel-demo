package cn.itcast.hotel.vo;

import cn.itcast.hotel.pojo.HotelDoc;
import lombok.Data;

import java.util.List;

@Data
public class pageResult {
    private Long total;
    private List<HotelDoc> hotels;   //注意返回值类型中 返回参数为hotels ，要保持一致

    public pageResult(Long total, List<HotelDoc> hotelList) {
        this.total = total;
        this.hotels = hotelList;
    }
}
