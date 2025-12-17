package cn.hjw.dev.platform.infrastructure.dcc;

import cn.hjw.dev.platform.types.annotations.DCCValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 动态配置中心
 * 通过http协议动态调整系统的配置参数
 */
@Slf4j
@Service
public class DynamicConfigCenter {

    // 1. 降级开关 (String类型，最基础)
    @Getter
    @DCCValue(value = "downgradeSwitch:0", description = "活动降级开关 (0-关闭 1-开启)")
    private volatile String downgradeSwitch = "0";

    // 2. 切量范围 (数值类型)
    @Getter
    @DCCValue(value = "cutRange:100", description = "流量切分范围 (0-100)")
    private volatile String cutRange = "100";

    // 3. 黑名单 (列表类型)
    @Getter
    @DCCValue(value = "scBlacklist:s02c02,s03c03", description = "渠道黑名单 (逗号分隔)")
    private volatile String scBlacklist = "s02c02,s03c03";

    // 体验 Token 开关及配置 (新增)
    @Getter
    @DCCValue(value = "demoTokenSwitch:0", description = "演示Token开关 (0-关闭 1-开启)")
    private volatile String demoTokenSwitch = "0";

    @Getter
    @DCCValue(value = "demoTokenSecret:Bearer experience-token-vip-888", description = "演示专用Token串")
    private volatile String demoTokenSecret = "experience-token-vip-888";

    @Getter
    @DCCValue(value = "demoUserId:test_user_001", description = "演示用户ID，可以自定义表示不同用户")
    private volatile String demoUserId = "7736117232733921470425906184967298691211750310624021055982293696";

    public boolean isDemoTokenOpen() {
        return "1".equals(demoTokenSwitch);
    }

    /**
     * 业务逻辑封装
     */
    public boolean isDowngrade() {
        log.info("DCC动态配置-降级开关：{}", downgradeSwitch);
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        log.info("DCC动态配置-限流范围：{}", cutRange);
        int hashCode = Math.abs(userId.hashCode());
        int range = Integer.parseInt(cutRange);
        return (hashCode % 100) <= range; // true 在范围内、false 不在范围内，默认是用户最后2位在100范围内允许访问
    }

    public boolean isSCBlackIntercept(String source, String channel) {
        log.info("DCC动态配置-黑名单拦截渠道：{}", scBlacklist);
        String sc = source + channel;
        return scBlacklist.contains(sc); // true 拦截、false 放行
    }
}
