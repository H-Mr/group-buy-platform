package cn.hjw.dev.platform.domain.activity.service.trial.processor;

import cn.hjw.dev.platform.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SwitchCheckProcessor implements DAGNodeProcessor<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public MarketDAGFactory.TrialNodeResult process(MarketProductEntity request, MarketDAGFactory.TrialContext context) throws Exception {
        log.info("DAG节点-营销试算开关校验: userId:{}", request.getUserId());

        // 降级开关校验
        if (activityRepository.downgradeSwitch()) {
            // 如果降级开关打开，则抛出降级异常，终止试算流程
            throw new AppException(ResponseCode.DOWNGRADE.getCode(), ResponseCode.DOWNGRADE.getInfo());
        }
        // 并发控制校验
        if (!activityRepository.cutRange(request.getUserId())) {
            // 如果用户不在允许的并发范围内，则抛出并发控制异常，终止试算流程
            throw new AppException(ResponseCode.CNTRANGE.getCode(), ResponseCode.CNTRANGE.getInfo());
        }

        return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_SWITCH_CHECK, true);
    }
}
