package cn.hjw.dev.mall.application.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Redis 客户端，使用 Redisson <a href="https://github.com/redisson/redisson">Redisson</a>
 *
 */
@Configuration
@EnableConfigurationProperties(RedisClientConfigProperties.class)
public class RedisClientConfig {

    @Bean("redissonClient")
    public RedissonClient redissonClient(ConfigurableApplicationContext applicationContext, RedisClientConfigProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort()) // redis://host:port
//              .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize()) // 连接池大小
                .setConnectionMinimumIdleSize(properties.getMinIdleSize()) // 最小空闲连接数
                .setIdleConnectionTimeout(properties.getIdleTimeout()) // 最大空闲时间
                .setConnectTimeout(properties.getConnectTimeout()) // 连接超时时间
                .setRetryAttempts(properties.getRetryAttempts()) // 连接重试次数
                .setRetryInterval(properties.getRetryInterval()) // 连接重试间隔时间
                .setPingConnectionInterval(properties.getPingInterval()) // 连接检查间隔时间
                .setKeepAlive(properties.isKeepAlive()) // 是否保持长连接
        ;

        return Redisson.create(config);
    }

    static class RedisCodec extends BaseCodec {

        private final Encoder encoder = in -> {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                JSON.writeJSONString(os, in, SerializerFeature.WriteClassName);
                return os.buffer();
            } catch (IOException e) {
                out.release();
                throw e;
            } catch (Exception e) {
                out.release();
                throw new IOException(e);
            }
        };

        private final Decoder<Object> decoder = (buf, state) -> JSON.parseObject(new ByteBufInputStream(buf), Object.class);

        @Override
        public Decoder<Object> getValueDecoder() {
            return decoder;
        }

        @Override
        public Encoder getValueEncoder() {
            return encoder;
        }

    }

}
