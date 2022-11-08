package cn.itcast.hotel.controller;


import cn.itcast.hotel.pojo.params.RequestParam;
import cn.itcast.hotel.service.IHotelService;
import cn.itcast.hotel.vo.filterResult;
import cn.itcast.hotel.vo.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/hotel")
@RestController
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @PostMapping("/list")
    public pageResult search(@RequestBody RequestParam requestParam) throws IOException {
        return hotelService.search(requestParam);
    }

    @PostMapping("/filters")
    public filterResult getFilters(@RequestBody RequestParam requestParam) throws IOException {
        return hotelService.getFilters(requestParam);
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@org.springframework.web.bind.annotation.RequestParam("key") String prefix){
        return hotelService.getSuggestions(prefix);
    }

}
