package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.ISkuDao;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import cn.hjw.dev.platform.infrastructure.sse.SseSessionManager;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.List;

/**
 * 营销查询服务
 * 职责：提供商品列表、活动详情的只读访问
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/query/")
public class MarketQueryController {

    @Resource
    private ISkuDao skuDao;

    @Resource
    private IGroupBuyActivityDao activityDao;

    @Resource
    private SseSessionManager sseSessionManager;

    @Resource
    private JwtUtils jwtUtils; // 注入工具类

    /**
     * SSE 接收拼团结果通知
     * SSE 是“监听通道”，下单是“业务动作”，两者必须分开，通常是先监听，后下单（防止下单太快，还没连上SSE就拼团成功了导致漏接消息）。
     * 前端调用: const es = new EventSource("/api/v1/query/sse/subscribe?token=" + your_jwt_token);
     */
    @GetMapping(value = "sse/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestParam("token") String token) {
        try {
            // 1. 校验 Token 非空
            if (StringUtils.isBlank(token)) {
                log.warn("SSE连接失败: Token为空");
                return null;
            }

            // 2. 解析 Token 获取 userId (这是最关键的一步，从加密串中拿 ID，不可伪造)
            Claims claims = jwtUtils.parseToken(token);
            if (claims == null) {
                log.warn("SSE连接失败: Token解析为空");
                return null;
            }

            String userId = (String) claims.get("userId"); // 确保 key 和生成时一致
            if (StringUtils.isBlank(userId)) {
                log.warn("SSE连接失败: Token中无UserId");
                return null;
            }

            // 3. 建立连接 (使用解析出来的 userId)
            return sseSessionManager.connect(userId);

        } catch (Exception e) {
            log.error("SSE连接鉴权异常", e);
            // 这里可以返回一个会立马报错的 Emitter，或者直接抛异常让前端重连
            return null;
        }
    }

    /**
     * 查询所有商品列表
     * 对应需求：浏览sku也不需要token
     */
    @GetMapping("query_sku_list")
    public Response<List<Sku>> querySkuList() {
        try {
            // 假设 Dao 层有 queryAll 或类似方法，如果没有需要在 XML 中补充
            // 这里演示调用 logic，实际需确保 Mapper 存在 select * from sku
            List<Sku> skus = skuDao.querySkuList();
            return Response.<List<Sku>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(skus)
                    .build();
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return Response.<List<Sku>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }

    /**
     * 查询活动列表
     */
    @GetMapping("query_activity_list")
    public Response<List<GroupBuyActivity>> queryActivityList() {
        try {
            List<GroupBuyActivity> activities = activityDao.queryGroupBuyActivityList();
            return Response.<List<GroupBuyActivity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(activities)
                    .build();
        } catch (Exception e) {
            log.error("查询活动列表失败", e);
            return Response.<List<GroupBuyActivity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }
}