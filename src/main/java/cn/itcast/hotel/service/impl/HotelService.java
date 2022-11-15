package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.params.RequestParam;
import cn.itcast.hotel.service.IHotelService;
import cn.itcast.hotel.vo.filterResult;
import cn.itcast.hotel.vo.pageResult;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private IHotelService hotelService;

    @Override
    public pageResult search(RequestParam param) {
        try {
//        1.创建request对象
            SearchRequest request = new SearchRequest("hotel");

//        2.准备DSL
            buildBasicQuery(param, request);//根据param中参数进行查询封装为DSL并接受request
//        3.发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public filterResult getFilters(RequestParam requestParam) {
        try {
            //        创建request对象
            SearchRequest request = new SearchRequest("hotel");

//        准备query （因为要对搜索字段进行 判定后再  过滤菜单选项）
            buildBasicQuery(requestParam, request);
//        进行聚合
            buildAggregation(request);
//        解析聚合
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//            返回解析后的集合结果
            return parseResponse(requestParam, response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        try {
            //      准备请求
            SearchRequest request = new SearchRequest("hotel");
//        准备请求参数
            request.source().suggest(new SuggestBuilder().addSuggestion(
                            "hotelSuggestion",
                            //注意这里获取值要和 search到的文档中的suggestion字段对应
                            SuggestBuilders.completionSuggestion("suggestion")
                                    .prefix(prefix)          //需要补充的前缀
                                    .skipDuplicates(true)    //忽略重复项
                                    .size(10)
                    )
            );

//        发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//            解析response并封装返回
            return parseSuggestions(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> parseSuggestions(SearchResponse response) {
//        获取处理结果
        Suggest suggest = response.getSuggest();
//        根据名称获取suggestion，与上面自定义的名称一致就可以
        CompletionSuggestion hotelSuggestion = suggest.getSuggestion("hotelSuggestion");
//        获取option!!!!!!!!!
        List<CompletionSuggestion.Entry.Option> options = hotelSuggestion.getOptions();
//        遍历以获取所有补全结果
        List<String> suggestions = new ArrayList<>(options.size());
        for (CompletionSuggestion.Entry.Option option : options) {
//            suggestion中存储对应的需要补全的结果集
            suggestions.add(option.getText().string());
        }
        return suggestions;
    }

    public filterResult parseResponse(RequestParam requestParam, SearchResponse response) {
        HashMap<String, List<String>> result = new HashMap<>();
        Aggregations aggregations = response.getAggregations();
        result.put("brand", getAggsByName(aggregations, "brand_agg"));
        result.put("city", getAggsByName(aggregations, "city_agg"));
        result.put("starName", getAggsByName(aggregations, "starName_agg"));
        return new filterResult(result);
    }

    public List<String> getAggsByName(Aggregations aggregations, String aggName) {
//        获取集合结果
        Terms terms = aggregations.get(aggName);
//        获取桶
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
//        封装对应的key存入list
        List<String> keyList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            keyList.add(bucket.getKeyAsString());
        }
        return keyList;
    }

    private void buildAggregation(SearchRequest request) {
        //        准备DSL （不查询文档）
        request.source().size(0);
//        为品牌名准备 过滤
        request.source().aggregation(AggregationBuilders
                .terms("brand_agg")
                .field("brand")
                .size(100));
//        为星级
        request.source().aggregation(AggregationBuilders
                .terms("starName_agg")
                .field("starName")
                .size(100));
//        为城市设置过滤
        request.source().aggregation(AggregationBuilders
                .terms("city_agg")
                .field("city")
                .size(100));
    }


    public pageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
//        获取总条数
        Long total = searchHits.getTotalHits().value;
//        获取查询文档
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotelDocList = new ArrayList<>();
        for (SearchHit hit : hits) {
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            //        获取排序值
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                Object sortValue = sortValues[0];
                hotelDoc.setDistance(sortValue);
            }
            hotelDocList.add(hotelDoc);
        }

//        封装数据并返回
        return new pageResult(total, hotelDocList);
    }

    /**
     * 在该方法中堆参数列表进行查询封装为request并返回
     *
     * @param param
     * @param request
     * @return
     */
    public void buildBasicQuery(RequestParam param, SearchRequest request) {
        //        1.1 创建query对象
        BoolQueryBuilder query = new BoolQueryBuilder();

        if (!("".equals(param.getKey())) && param.getKey() != null) {
//                如果key不为空
            query.must(QueryBuilders.matchQuery("all", param.getKey()));
        }
        if (!("".equals(param.getBrand())) && param.getBrand() != null) {
//                品牌不为空
            query.filter(QueryBuilders.termQuery("brand", param.getBrand()));
        }
        if (!("".equals(param.getCity())) && param.getCity() != null) {
//                城市不为空
            query.filter(QueryBuilders.termQuery("city", param.getCity()));
        }
        if (!("".equals(param.getStarName())) && param.getStarName() != null) {
//                城市不为空
            query.filter(QueryBuilders.termQuery("starName", param.getStarName()));
        }
        if (param.getMinPrice() != null && param.getMaxPrice() != null) {
            query.filter(QueryBuilders.rangeQuery("price").gt(param.getMinPrice())
                    .lt(param.getMaxPrice()));
        }
//            为地理位置设置排序
        if (param.getLocation() != null && !"".equals(param.getLocation())) {
            request.source().sort(SortBuilders
                    .geoDistanceSort("location", new GeoPoint(param.getLocation()))
                    .order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS));
        }
        //        进行分页过滤
        Integer page = param.getPage();
        Integer size = param.getSize();
//        封装query到请求中去
        request.source().query(query)
                .from((page - 1) * size).size(size);

        // 2.算分控制
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        // 原始查询，相关性算分的查询
                        query,
                        // function score的数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                // 其中的一个function score 元素
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        // 过滤条件
                                        QueryBuilders.termQuery("isAD", true),
                                        // 算分函数
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        });
        request.source().query(functionScoreQuery);
    }



    @Override
    public void deleteById(Long id){
        try{
            //        对索引进行操作，删除一个文档
            // 1.准备Request
            DeleteIndexRequest request = new DeleteIndexRequest("hotel",id.toString());
            // 3.发送请求
            client.indices().delete(request, RequestOptions.DEFAULT);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertById(Long id) {
        try{
//            对索引进行操作，新增一个文档（因为根据索引建立起来的文档只能新增而不能修改，所以这里采取删除一个文档再新增）
            //           2.准备数据
            Hotel hotel = hotelService.getById(id);
            HotelDoc hotelDoc = new HotelDoc(hotel);
//            1.准备Request
            IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
//            2.准备Json文档 向ES插入数据，必须将数据转换位JSON格式
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);

//            3.发送请求
            client.index(request,RequestOptions.DEFAULT);
            System.out.println("进行了修改:"+hotelDoc);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
