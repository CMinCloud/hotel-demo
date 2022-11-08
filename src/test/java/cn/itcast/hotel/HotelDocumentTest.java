package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class HotelDocumentTest {


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

    //    创建文档
    @Test
    void addDocument() throws IOException {
//      根据酒店id获取酒店信息
        Hotel hotel = hotelService.getById(395702L);
        HotelDoc hotelDoc = new HotelDoc(hotel);   // 封装为文档数据
        String jsonString = JSON.toJSONString(hotelDoc);
        System.out.println(jsonString);

//        设置查询请求 - 创建IndexRequest，指定索引库名和id
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
//        准备json文档
        request.source(jsonString, XContentType.JSON);
//        发送文档
        client.index(request, RequestOptions.DEFAULT);
    }

    //    获取文档
    @Test
    void getDocument() throws IOException {

        GetRequest getRequest = new GetRequest("hotel", "395702");
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
//        解析响应结果
        String source = response.getSourceAsString();
//        重新转回对象
        HotelDoc hotelDoc = JSON.parseObject(source, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    //    删除文档
    @Test
    void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "395702");
        DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
//        解析响应结果
        Object o = JSON.parse(String.valueOf(deleteResponse));
        System.out.println(o);
    }

    //    修改文档
    @Test
    void UpdateDocument() throws IOException {
        // 1.准备Request
        UpdateRequest request = new UpdateRequest("hotel", "395702");
        // 2.准备请求参数
        request.doc(
                "price", "952",
                "starName", "四钻"
        );
        // 3.发送请求
        client.update(request, RequestOptions.DEFAULT);
    }

//    批量添加文档
    @Test
    void testBulkRequest() throws IOException {
        List<Hotel> list = hotelService.list();
//        创建request：利用JavaRestClient中的BulkRequest批处理，实现批量新增文档
        BulkRequest bulkRequest = new BulkRequest();
        for (Hotel hotel : list) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
//            .创建新增文档的Request对象  ，采用链式编程
            bulkRequest.add(new IndexRequest("hotel").id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(bulkRequest,RequestOptions.DEFAULT);
    }

/*    @Test
    void testAddDocument() throws IOException {
        // 1.查询数据库hotel数据
        Hotel hotel = hotelService.getById(61083L);
        // 2.转换为HotelDoc
        HotelDoc hotelDoc = new HotelDoc(hotel);
        // 3.转JSON
        String json = JSON.toJSONString(hotelDoc);

        // 1.准备Request
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        // 2.准备请求参数DSL，其实就是文档的JSON字符串
        request.source(json, XContentType.JSON);
        // 3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocumentById() throws IOException {
        // 1.准备Request      // GET /hotel/_doc/{id}
        GetRequest request = new GetRequest("hotel", "61083");
        // 2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3.解析响应结果
        String json = response.getSourceAsString();

        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println("hotelDoc = " + hotelDoc);
    }

    @Test
    void testDeleteDocumentById() throws IOException {
        // 1.准备Request      // DELETE /hotel/_doc/{id}
        DeleteRequest request = new DeleteRequest("hotel", "61083");
        // 2.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateById() throws IOException {
        // 1.准备Request
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        // 2.准备参数
        request.doc(
                "price", "870"
        );
        // 3.发送请求
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulkRequest() throws IOException {
        // 查询所有的酒店数据
        List<Hotel> list = hotelService.list();

        // 1.准备Request
        BulkRequest request = new BulkRequest();
        // 2.准备参数
        for (Hotel hotel : list) {
            // 2.1.转为HotelDoc
            HotelDoc hotelDoc = new HotelDoc(hotel);
            // 2.2.转json
            String json = JSON.toJSONString(hotelDoc);
            // 2.3.添加请求
            request.add(new IndexRequest("hotel").id(hotel.getId().toString()).source(json, XContentType.JSON));
        }

        // 3.发送请求
        client.bulk(request, RequestOptions.DEFAULT);
    }*/


}
