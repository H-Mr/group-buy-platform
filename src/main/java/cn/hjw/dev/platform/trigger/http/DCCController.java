package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dcc.DccValueManager;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 动态配置控制器
 * 职责：提供动态配置变更的HTTP接口
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/gbm/dcc/")
public class DCCController {

    @Resource
    private RTopic dccTopic;

    @Resource
    private DccValueManager dccValueManager;

    /**
     * 动态更新配置接口
     */
    @GetMapping("update_config")
    public Response<Boolean> updateConfig(@RequestParam("key") String key, @RequestParam("value") String value) {
        try {
            log.info("DCC 请求变更: {} -> {}", key, value);
            // 发布到 Redis，触发所有节点更新
            dccTopic.publish(key + "," + value);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("DCC 变更失败", e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("变更失败")
                    .build();
        }
    }

    /**
     *查询所有配置接口
     */
    @GetMapping("query_configs")
    public Response<Map<String, DccValueManager.DccMetaInfo>> queryConfigs() {
        return Response.<Map<String, DccValueManager.DccMetaInfo>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(dccValueManager.getAllConfigs())
                .build();
    }
}