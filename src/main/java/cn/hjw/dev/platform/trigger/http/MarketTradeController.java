package cn.hjw.dev.platform.trigger.http;


import cn.hjw.dev.platform.api.IMarketTradeService;
import cn.hjw.dev.platform.api.dto.LockMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.LockMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.ITradeLockOrderService;
import cn.hjw.dev.platform.domain.trade.service.ITradeSettlementOrderService;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import cn.hjw.dev.platform.types.utils.UserContext;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 扩展使用
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {

    @Resource
    private ITradeLockOrderService lockMarketPayOrder;

    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    /**
     * 通过HTTP调用锁单
     * @param lockMarketPayOrderRequestDTO
     * @return
     */
    @PostMapping("lock_market_pay_order")
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {
        try {
            // 参数
            String userId = lockMarketPayOrderRequestDTO.getUserId();; // 当前的用户id
            String source = lockMarketPayOrderRequestDTO.getSource(); // 来源
            String channel = lockMarketPayOrderRequestDTO.getChannel(); // 渠道
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId(); // 下单的商品id
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId(); // 拼团活动id
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo(); // 外部系统交易单号
            String teamId = lockMarketPayOrderRequestDTO.getTeamId(); // 参与的队伍id，可以为空，表示新团锁单
            String notifyUrl = lockMarketPayOrderRequestDTO.getNotifyUrl(); // 平团系统返回外部系统的回调地址

            log.info("=================系统收到锁单请求:{}=========================", JSON.toJSONString(lockMarketPayOrderRequestDTO));

            // 拼团id可以为空，表示新团锁单
            if(StringUtils.isAnyBlank(userId,source,channel,goodsId,outTradeNo,notifyUrl) || Objects.isNull(activityId)) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 查询当前用户是否存在未支付的锁单记录
            MarketPayOrderEntity pendingPayLockOrder = lockMarketPayOrder.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
            if (ObjectUtils.isNotEmpty(pendingPayLockOrder)) {
                // 存在未支付的锁单记录，直接返回
                log.info("交易锁单记录(存在):{} pendingPayLockOrder:{}", userId, JSON.toJSONString(pendingPayLockOrder));
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                        .orderId(pendingPayLockOrder.getOrderId())
                        .deductionPrice(pendingPayLockOrder.getDeductionPrice())
                        .tradeOrderStatus(pendingPayLockOrder.getTradeOrderStatusEnumVO().getCode())
                        .teamId(teamId)
                        .build();
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(lockMarketPayOrderResponseDTO)
                        .build();
            }

            // 构造一个锁单记录
            TradeLockRequestEntity tradeLockRequestEntity = TradeLockRequestEntity.builder()
                    .userId(userId)
                    .source(source)
                    .channel(channel)
                    .goodsId(goodsId)
                    .outTradeNo(outTradeNo)
                    .activityId(activityId)
                    .teamId(teamId)
                    .notifyUrl(notifyUrl)
                    .build();
            // 2. 锁单
            pendingPayLockOrder = lockMarketPayOrder.lockMarketPayOrder(tradeLockRequestEntity);

            log.info("=================系统锁单成功:{}=========================", JSON.toJSONString(pendingPayLockOrder));

            // 3.返回结果
            LockMarketPayOrderResponseDTO responseDTO = LockMarketPayOrderResponseDTO.builder()
                    .orderId(pendingPayLockOrder.getOrderId())
                    .deductionPrice(pendingPayLockOrder.getDeductionPrice())
                    .tradeOrderStatus(pendingPayLockOrder.getTradeOrderStatusEnumVO().getCode())
                    .payPrice(pendingPayLockOrder.getPayPrice()) // 支付价格
                    .teamId(pendingPayLockOrder.getTeamId())
                    .build();

            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();

        } catch (AppException e) {
            log.error("营销交易锁单业务异常:{} LockMarketPayOrderRequestDTO:{}", UserContext.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败:{} LockMarketPayOrderRequestDTO:{}", UserContext.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 通过HTTP调用结算
     */
    @PostMapping(value = "settlement_market_pay_order")
    @Override
    public Response<SettlementMarketPayOrderResponseDTO> settleMarketPayOrder(SettlementMarketPayOrderRequestDTO requestDTO) {
        try {
            log.info("营销交易组队结算开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());

            if (StringUtils.isAnyBlank(requestDTO.getUserId(),requestDTO.getSource(),requestDTO.getChannel(),requestDTO.getOutTradeNo()) || ObjectUtils.isEmpty(requestDTO.getOutTradeTime()) ) {
                return Response.<SettlementMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 1. 结算服务
            TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(TradePaySuccessEntity.builder()
                    .source(requestDTO.getSource())
                    .channel(requestDTO.getChannel())
                    .userId(requestDTO.getUserId())
                    .outTradeNo(requestDTO.getOutTradeNo())
                    .outTradeTime(requestDTO.getOutTradeTime())
                    .build());

            SettlementMarketPayOrderResponseDTO responseDTO = SettlementMarketPayOrderResponseDTO.builder()
                    .userId(tradePaySettlementEntity.getUserId())
                    .teamId(tradePaySettlementEntity.getTeamId())
                    .activityId(tradePaySettlementEntity.getActivityId())
                    .outTradeNo(tradePaySettlementEntity.getOutTradeNo())
                    .build();

            // 返回结果
            Response<SettlementMarketPayOrderResponseDTO> response = Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();

            log.info("营销交易组队结算完成:{} outTradeNo:{} response:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo(), JSON.toJSONString(response));

            return response;
        } catch (AppException e) {
            log.error("营销交易组队结算异常:{} LockMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e) {
            log.error("营销交易组队结算失败:{} LockMarketPayOrderRequestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO), e);
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}