package cn.hjw.dev.platform.infrastructure.gateway;

import cn.hjw.dev.platform.domain.inventory.event.InventoryChangedEventType;
import cn.hjw.dev.platform.domain.inventory.model.valobj.InventoryChangedTypeVO;
import cn.hjw.dev.platform.infrastructure.sse.SseSessionManager;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
* 拼团回调服务，调用okHttp发送回调信息
 * @create 2025-01-31 09:12
 */
@Slf4j
@Service
public class GroupBuyNotifyGateway {

    @Resource
    private OkHttpClient okHttpClient;

    @Resource
    private InventoryChangedEventType inventoryChangedEventType;

    @Resource
    private SseSessionManager sseSessionManager;

    /**
     * 正常是通过http回调拼团服务端接口
     * @param apiUrl
     * @param notifyRequestDTOJSON
     * @return
     * @throws Exception
     */
    public String groupBuyNotify(String apiUrl, String notifyRequestDTOJSON) throws Exception {
        try {
            // 1. 构建参数
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json;charset=utf-8"),
                    notifyRequestDTOJSON
            );
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(requestBody)
                    .header("content-type", "application/json;charset=utf-8")
                    .build();

            // 2. 调用接口
            // Response 需要导入 okhttp3.Response，使用 try-with-resources 确保关闭
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.body() == null) {
                    throw new IllegalStateException("response body is null");
                }
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("拼团回调 HTTP 接口服务异常 {}", apiUrl, e);
            throw new AppException(ResponseCode.HTTP_EXCEPTION);
        }
    }

    public String SSEGroupBuyNotify(String notifyRequestDTOJSON) {

        ObjectMapper mapper = new ObjectMapper(); // Jackson核心工具类

       try {
           // 1. 解析JSON字符串为JsonNode（根节点）
           JsonNode rootNode = mapper.readTree(notifyRequestDTOJSON);
           String teamId = rootNode.path("teamId").asText();// 获取 teamId 字段值
            // 2. 获取 userIdList 列表（从JsonNode转成Java的List<String>）
           ArrayNode userIdListNode = (ArrayNode) rootNode.path("userIdList"); // 安全获取数组（不存在则返回空ArrayNode）
           List<String> userIdList = new ArrayList<>();
           userIdListNode.forEach(userNode -> userIdList.add(userNode.asText()));
           // 3. 遍历userIdList，获取每个userId对应的商品ID列表
           for(String userId : userIdList) {
               sseSessionManager.sendMessage(userId, new String(String.format("拼团成功！成团ID: %s", teamId).getBytes(), StandardCharsets.UTF_8));
               log.info("SSE 回调通知！ userId: {} 拼团成功！ 团号：{}",userId,teamId);
               // 扣减库存
               ArrayNode goodsIdArrayNode  = (ArrayNode) rootNode.path(userId); // 安全获取数组（不存在则返回空ArrayNode）
               goodsIdArrayNode.forEach(goodsIdNode -> inventoryChangedEventType.publishInventoryChangedEvent(
                       goodsIdNode.asText(),InventoryChangedTypeVO.DECREASE,1
               ));
           }
       } catch (Exception e) {
              log.error("拼团回调 SSE 接口服务异常 {}", notifyRequestDTOJSON, e);
              return "error";
       }
        return "success";
    }

}
