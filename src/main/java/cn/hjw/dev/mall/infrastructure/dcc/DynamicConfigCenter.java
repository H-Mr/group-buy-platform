package cn.hjw.dev.mall.infrastructure.dcc;

import cn.hjw.dev.types.annotations.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DynamicConfigCenter {

    @DCCValue("downgradeSwitch=0")
    private String downgradeSwitch;

    @DCCValue("cutRange=1")
    private String cutRange;

    @DCCValue("scBlacklist=s02c02")
    private String scBlacklist;

    public boolean isDowngrade() {
        log.info("DCC动态配置-降级开关：{}", downgradeSwitch);
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        log.info("DCC动态配置-限流范围：{}", cutRange);
        int hashCode = Math.abs(userId.hashCode());
        hashCode %= 100; // 0-99
        log.info("用户ID：{}，Hash后值：{}", userId, hashCode);
        return hashCode <= Integer.parseInt(cutRange);
    }

    public boolean isSCBlackIntercept(String source, String channel) {
        log.info("DCC动态配置-黑名单拦截渠道：{}", scBlacklist);
        String sc = source + channel;
        List<String> blackList = Arrays.asList(scBlacklist.split(","));
        return blackList.contains(sc); // true 拦截、false 放行
    }
}
