package cn.itcast.hotel;


import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class HotelSearchTest {

    @Autowired
    private IHotelService hotelService;

    private RestHighLevelClient client;

    @BeforeEach    //在每一个服务前初始化
    public void setup() {
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://47.109.59.20:9200")));
    }

    @AfterEach
    public void close() throws IOException {
        this.client.close();
    }

//  matchall查询
    @Test
    void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
//        2.组织dsl参数:查询所有
//        request.source().query(QueryBuilders.matchAllQuery());
        request.source().query(new MatchAllQueryBuilder());
//        3.发送请求    ，得到响应结果
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        解析响应结果
        handleResponse(response);
    }

    @Test
    void handleResponse(SearchResponse response){
        SearchHits searchHits = response.getHits();  //命中结果（封装了查询到的数据）
        // 4.1.获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("获取的总条数："+total);
        // 4.2.文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3.遍历
        for (SearchHit hit : hits) {
            // 获取文档source
            String sourceAsString = hit.getSourceAsString();
            // 反序列化
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            System.out.println("hotelDoc: "+hotelDoc);
        }
    }

//    match查询
    @Test
    void testMatch() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        // 2.准备DSL
        request.source()
                .query(QueryBuilders.matchQuery("all", "如家"));
//        查询字段过多效率低，最好通过copy to 设置一个 集合字段
//        request.source().query(QueryBuilders.multiMatchQuery("如家","name","business"));
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

//    布尔查询（精确查询term + 范围查询 range）  ，应该是用的最多的
    @Test
    void testBool() throws IOException {
        // 1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        // 2.准备DSL
        request.source().query(
                QueryBuilders.boolQuery().
                must(QueryBuilders.termQuery("name","如家"))      //bool添加must条件 （精确查询）
                .filter(QueryBuilders.rangeQuery("price").gt(380)));   // 添加filter条件 （筛选范围）

        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    //排序、分页和query同级，所以直接用source()后接参数就可以
    @Test
    void testPageAndSort() throws IOException {
        // 页码，每页大小
        int page = 1, size = 5;

        // 1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        // 2.准备DSL
        // 2.1.query
        request.source().query(QueryBuilders.matchAllQuery());
        // 2.2.排序 sort
        // 2.3.分页 from、size
        request.source().sort("price", SortOrder.ASC).from((page - 1) * size).size(5);
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    @Test
    void testHighlight() throws IOException {
//        1.准备request
        SearchRequest request = new SearchRequest("hotel");
//        2.准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"));
//        设置高亮字段为名称（同时）
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        解析响应
        handleResponse(response);
    }
}
