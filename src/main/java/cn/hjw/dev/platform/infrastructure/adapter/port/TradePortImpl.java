package cn.hjw.dev.platform.infrastructure.adapter.port;

import cn.hjw.dev.platform.domain.trade.adapter.port.ITradePort;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;
import cn.hjw.dev.platform.infrastructure.gateway.GroupBuyNotifyGateway;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import cn.hjw.dev.platform.types.enums.NotifyTaskHTTPEnumVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @description
 * @create 2025-01-31 13:34
 */
@Service
public class TradePortImpl implements ITradePort {

    @Resource
    private GroupBuyNotifyGateway groupBuyNotifyGateway; // 拼团回调服务,通过okhttp调用回调接口
    @Resource
    private IRedisService redisService; // Redis 分布式锁服务,防止多实例重复执行任务

    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
            RLock lock = redisService.getLock(notifyTask.lockKey());
            try {
                // group-buy-market 拼团服务端会被部署到多台应用服务器上，那么就会有很多任务一起执行。这个时候要进行抢占，避免被多次执行
                if (lock.tryLock(3, 0, TimeUnit.SECONDS)) {
                    /*
lock.tryLock(3, 0, TimeUnit.SECONDS)
第一个参数 3：最多等待获取锁的时间（这里是 3 秒），超时返回 false。
第二个参数 0：锁的租约时间（获取锁后自动释放的时长）。在 Redisson 中传 0（或小于等于 0）通常表示使用看门狗机制（watchdog），即不按固定时间自动释放，Redisson 会周期性续期，直到显式 unlock()。
第三个参数 TimeUnit.SECONDS：时间单位（秒）。
                    * */
                    try {
                        // 无效的 notifyUrl 则直接返回成功
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                            return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                        }
                        // 分布式系统使用http接口回调
                        // groupBuyNotifyGateway.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                        return groupBuyNotifyGateway.SSEGroupBuyNotify(notifyTask.getParameterJson());
                    } finally {
                        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                            // isLocked() 表示锁当前是否被某个线程持有（任意线程）。
                            // isHeldByCurrentThread() 表示当前线程是否持有该锁。
                            // 免在锁不存在或被其他线程持有时调用 unlock() 导致 IllegalMonitorStateException。
                            // 关于“其他线程”：
                            //既包括同一 JVM/进程内的其他线程，也包括部署在其他服务器上的线程.
                            // 因为 Redisson 的锁信息存在 Redis 中，是分布式的
                            // Redisson 在 Redis 中记录了锁的持有者标识（通常含线程 id 和实例 id），所以“不是当前线程持有”可以是本地线程也可以是远端线程。
                            lock.unlock();
                        }
                    }
                }
                return NotifyTaskHTTPEnumVO.NULL.getCode();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return NotifyTaskHTTPEnumVO.NULL.getCode();
            }
    }

}
