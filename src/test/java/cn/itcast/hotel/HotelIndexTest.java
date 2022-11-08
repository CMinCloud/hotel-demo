package cn.itcast.hotel;


import cn.itcast.hotel.constants.HotelIndexConstants;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelIndexConstants.MAPPING_TEMPLATE;

@SpringBootTest
class HotelIndexTest {

    private RestHighLevelClient client;

/*    @Test
    void testCreateIndex() throws IOException {
        // 1.准备Request      PUT /hotel
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsIndex() throws IOException {
        // 1.准备Request
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 3.发送请求
        boolean isExists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(isExists ? "存在" : "不存在");
    }
    @Test
    void testDeleteIndex() throws IOException {
        // 1.准备Request
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 3.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }


    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.150.101:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }*/

    @BeforeEach    //在每一个服务前初始化
    public void setup(){
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://47.109.59.20:9200")));
    }


    @AfterEach
    public void close() throws IOException {
        this.client.close();
    }

//    创建索引库
    @Test
    public void create() throws IOException {
//        1.创建request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
//        2.添加请求参数，其实就是DSL的JSON参数部分
        request.source(HotelIndexConstants.MAPPING_TEMPLATE,XContentType.JSON);
//        3.发送创建索引的请求：client.indices()方法的返回值是IndicesClient类型，封装了所有与索引库操作有关的方法
       /* IndicesClient indices = client.indices();
        indices.create(request,RequestOptions.DEFAULT);*/
        client.indices().create(request,RequestOptions.DEFAULT);  //支持链式编程
    }

//    查看索引
    public void getIndex(){
/*        GetIndexRequest request = new GetIndexRequest("hotel");
        client.indices().getIndexTemplate(request,RequestOptions.DEFAULT)*/

    }

//    删除索引
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request,RequestOptions.DEFAULT);
    }

//    判断索引是否存在
    @Test
    public void checkExit() throws IOException {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.err.println(exists ? "索引库存在":"索引库不存在");
    }


}
