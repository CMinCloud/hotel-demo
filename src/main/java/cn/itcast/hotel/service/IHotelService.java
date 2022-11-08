package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.params.RequestParam;
import cn.itcast.hotel.vo.filterResult;
import cn.itcast.hotel.vo.pageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;


public interface IHotelService extends IService<Hotel> {
    /**
     * 根据关键字搜索酒店信息
     * @param requestParam 请求参数对象，包含用户输入的关键字
     * @return 酒店文档列表
     */
    pageResult search(RequestParam requestParam) throws IOException;

    /**
     * 根据搜索关键字过滤 搜索菜单栏
     * @param requestParam
     * @return  搜索菜单栏数据
     */
    filterResult getFilters(RequestParam requestParam);

    /**
     * 在搜索框根据输入字段自动补全
     * @param prefix
     * @return
     */
    List<String> getSuggestions(String prefix);

    /**
     * 根据监听进行crud操作
     */
    void deleteById(Long id);

    void insertById(Long id);
}
