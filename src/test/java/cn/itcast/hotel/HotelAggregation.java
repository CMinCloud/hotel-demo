package cn.itcast.hotel;


import cn.itcast.hotel.service.IHotelService;
import org.apache.http.HttpHost;
import org.apache.lucene.index.Term;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelAggregation {

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

    @Test
    void testSetAggs() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        request.source().size(0);// 不查询文档
        request.source().aggregation(AggregationBuilders.terms("brand_agg")
                .field("brand").size(20));

//        发出请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        解析结果
//        System.out.println(response);
        parseResponse(response);

    }

//    聚合的结果也与查询结果不同，API也比较特殊。不过同样是JSON逐层解析：

    void parseResponse(SearchResponse response) {
        Aggregations aggregations = response.getAggregations();
//        根据名称获取聚合结果
        Terms brandTerms = aggregations.get("brand_agg");
//        获取桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
//            获取key，也就是品牌信息
            String key = bucket.getKeyAsString();
            System.out.println("key:"+key+",count:"+bucket.getDocCount());
        }
    }
}
