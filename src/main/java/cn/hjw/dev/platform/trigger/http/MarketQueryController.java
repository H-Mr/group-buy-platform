package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.ISkuDao;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import cn.hjw.dev.platform.infrastructure.dcc.DynamicConfigCenter;
import cn.hjw.dev.platform.infrastructure.sse.SseSessionManager;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

    // 1. 注入动态配置中心 (必须)
    @Resource
    private DynamicConfigCenter dynamicConfigCenter;

    /**
     * SSE 接收拼团结果通知
     * SSE 是“监听通道”，下单是“业务动作”，两者必须分开，通常是先监听，后下单（防止下单太快，还没连上SSE就拼团成功了导致漏接消息）。
     * 前端调用: const es = new EventSource("/api/v1/query/sse/subscribe?token=" + your_jwt_token);
     */
    @GetMapping(value = "sse/subscribe", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(@RequestParam("token") String token) {
        // 1. 校验 Token 非空
        if (StringUtils.isBlank(token)) {
            log.warn("SSE连接失败: Token为空");
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "token不能为空，SSE连接鉴权失败.");
        }
        String userId;
        // 2. 优先检查是否为演示/体验 Token (与 LoginInterceptor 逻辑保持一致)
        if (dynamicConfigCenter.isDemoTokenOpen() && token.equals(dynamicConfigCenter.getDemoTokenSecret())) {
            userId = dynamicConfigCenter.getDemoUserId();
            log.info("SSE使用体验Token连接，用户ID: {}", userId);
        } else {
            try {
                // 3. 如果不是演示Token，再尝试进行 JWT 解析
                Claims claims = jwtUtils.parseToken(token);
                if (ObjectUtils.isEmpty(claims)) {
                    log.warn("SSE连接失败: Token解析失败");
                    throw new AppException(ResponseCode.UN_ERROR.getCode(), "SSE连接鉴权失败: Token无效或已过期.");
                }
                userId = (String) claims.get("userId"); // 确保 key 和生成时一致
                if (StringUtils.isBlank(userId)) {
                    log.warn("SSE连接失败: Token中无UserId");
                    throw new AppException(ResponseCode.UN_ERROR.getCode(), "SSE连接鉴权失败: Token无效或已过期.");
                }
            } catch (Exception e) {
                log.warn("SSE连接失败: Token解析异常", e);
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "SSE连接鉴权失败: Token无效或已过期.");
            }
        }
        // 3. 建立连接 (使用解析出来的 userId)
        return sseSessionManager.connect(userId);
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