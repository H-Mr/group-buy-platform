package cn.hjw.dev.platform.types.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 这个事件体系是每个事件是单例的，代表事件类型，每次发布事件时，携带不同的Payload数据。
 * 事件总线（EventBus）根据事件载荷的类型分发给相应的监听器进行处理。
 * 这种设计使得事件类型和事件数据解耦，便于扩展
 * @param <T>
 */
@Data
public abstract class BaseEventType<T> {


    public Message<T> buildEventPayload(T payload) {
        return Message.<T>builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .payload(payload)
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message<T> {
        private String id;
        private LocalDateTime timestamp;
        private T payload;
    }

}
