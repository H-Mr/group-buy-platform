package cn.hjw.dev.platform.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseSessionManager {

    /**
     * 存储用户连接：Key = UserId, Value = SseEmitter
     */
    private final Map<String, SseEmitter> sessions = new ConcurrentHashMap<>();

    /**
     * 建立连接
     */
    public SseEmitter connect(String userId) {
        // 设置超时时间，0表示不过期（生产建议设置具体时间如 30分钟）
        SseEmitter emitter = new SseEmitter(0L);

        sessions.put(userId, emitter);
        log.info("SSE连接建立: userId={}", userId);

        // 注册回调：连接完成、超时或报错时移除
        emitter.onCompletion(() -> {
            sessions.remove(userId);
            log.info("SSE连接结束: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            sessions.remove(userId);
            log.info("SSE连接超时: userId={}", userId);
        });
        emitter.onError((e) -> {
            sessions.remove(userId);
            log.error("SSE连接错误: userId={}", userId, e);
        });

        return emitter;
    }



    /**
     * 发送消息
     */
    public void sendMessage(String userId, Object message) {
        try {
            SseEmitter emitter = sessions.getOrDefault(userId,null); // 获取连接
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("group_buy_success") // 事件名称，前端监听用
                            .data(message));
                    log.info("SSE消息推送成功: userId={}", userId);
                } catch (IOException e) {
                    log.warn("SSE消息推送失败，移除连接: userId={}", userId);
                    sessions.remove(userId);
                }
            } else {
                log.debug("用户不在线，消息忽略: userId={}", userId);
            }
        }catch (Exception e ) {
            log.error("SSE发送消息异常: userId={}", userId, e);
        }
    }

    /**
     * 定时心跳任务
     * 每 30 秒执行一次，防止 Nginx/网关 超时断开连接
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        if (sessions.isEmpty()) {
            return;
        }

        log.debug("开始发送SSE心跳，当前在线人数: {}", sessions.size());

        sessions.forEach((userId, emitter) -> {
            try {
                // 发送 SSE 注释 (Comment) 作为心跳
                // 格式为 ":ping\n\n"，浏览器 EventSource 会自动忽略以冒号开头的行，不会触发 onmessage
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (IOException e) {
                // 心跳发送失败，说明连接已失效
                log.warn("心跳发送失败，移除失效连接: userId={}", userId);
                sessions.remove(userId);
            } catch (Exception e) {
                // 捕获其他异常，防止单点故障影响整个循环
                log.error("心跳发送异常: userId={}", userId, e);
                sessions.remove(userId);
            }
        });
    }
}