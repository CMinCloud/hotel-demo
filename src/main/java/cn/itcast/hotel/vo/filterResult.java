package cn.itcast.hotel.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class filterResult {

    private HashMap<String, List<String>> filters;

    public filterResult(HashMap<String, List<String>> filters) {
        this.filters = filters;
    }
}
