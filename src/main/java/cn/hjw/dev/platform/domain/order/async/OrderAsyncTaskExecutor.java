package cn.hjw.dev.platform.domain.order.async;

import cn.hjw.dev.platform.domain.order.adapter.port.IOrderPort;
import cn.hjw.dev.platform.domain.order.model.valobj.SettlementMarketPayOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OrderAsyncTaskExecutor {

    @Resource
    private IOrderPort port;

    /**
     * 异步执行耗时操作（复用全局asyncTaskExecutor线程池）
     * 注：@Async方法不能是private（Spring AOP 无法代理）
     */
    @Async("asyncTaskExecutor") // 指定全局耗时操作线程池
    public void asyncDoSettlement(SettlementMarketPayOrderVO settlementVo) {
        try {
            // 耗时操作1：执行结算
            port.settlementMarketPayOrder(settlementVo);
        } catch (Exception e) {
            // 异常处理：日志+重试+告警
            log.error("订单{}耗时操作执行失败", settlementVo.getOrderId(), e);
        }
    }

}
