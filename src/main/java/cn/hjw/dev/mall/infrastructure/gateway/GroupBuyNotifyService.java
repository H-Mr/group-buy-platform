package cn.hjw.dev.mall.infrastructure.gateway;

import cn.hjw.dev.types.enums.ResponseCode;
import cn.hjw.dev.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* 拼团回调服务，调用okHttp发送回调信息
 * @create 2025-01-31 09:12
 */
@Slf4j
@Service
public class GroupBuyNotifyService {

    @Resource
    private OkHttpClient okHttpClient;

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

}
