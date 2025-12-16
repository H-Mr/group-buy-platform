package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.valobj.ActivityStatusEnumVO;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class TradeCheckProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) {

        log.info("DAG-Lock: 执行规则校验");
        GroupBuyActivityEntity activity = ctx.getActivity();

        // 1. 基础存在性与状态校验
        if (activity == null || !ActivityStatusEnumVO.EFFECTIVE.equals(activity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        // 2. 时间校验
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new AppException(ResponseCode.E0102);
        }

        // 3. 限购校验
        if (activity.getTakeLimitCount() != null && ctx.getUserTakeCount() >= activity.getTakeLimitCount()) {
            throw new AppException(ResponseCode.E0103);
        }

        // 4. 风控校验
        if (ctx.getRiskScore() != null && ctx.getRiskScore() > 80) {
            log.warn("用户风控拦截: score={}", ctx.getRiskScore());
            throw new AppException(ResponseCode.E0101.getCode(), "账号异常，禁止购买");
        }

        // 5. 拼团是否已满校验
        GroupBuyProgressVO progress = ctx.getGroupBuyProgress();
        if(progress != null && progress.getLockCount() >= progress.getTargetCount()) {
            log.warn("拼团已满，无法继续锁定: lockCount={}, targetCount={}", progress.getLockCount(), progress.getTargetCount());
            throw new AppException(ResponseCode.E0006);
        }

        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_CHECK, true);
    }
}
