package cn.hjw.dev.platform.infrastructure.adapter.port;

import cn.hjw.dev.platform.api.IMarketTradeService;
import cn.hjw.dev.platform.api.dto.LockMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.LockMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.order.adapter.port.IOrderPort;
import cn.hjw.dev.platform.domain.order.model.entity.ProductEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.GroupMarketProductPriceVO;
import cn.hjw.dev.platform.domain.order.model.valobj.LockMarketPayOrderVO;
import cn.hjw.dev.platform.domain.order.model.valobj.SettlementMarketPayOrderVO;
import cn.hjw.dev.platform.infrastructure.dao.ISkuDao;
import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import cn.hjw.dev.platform.infrastructure.gateway.AlipayRequestGateway;
import cn.hjw.dev.platform.infrastructure.gateway.ProductGateway;
import cn.hjw.dev.platform.infrastructure.gateway.dto.ProductDTO;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单与与外部服务通信适配器实现类
 * 当前为单体项目，直接调用服务或mock服务
 */
@Slf4j
@Component
public class OrderPortImpl implements IOrderPort {

    @Value("${app.config.group-buy-market.notify-url}")
    private String notifyUrl;

    /**
     * 商品服务 RPC 调用接口; 正常是通过 feign 或者 restTemplate 调用商品服务
     * 现在是单体项目，直接mock服务
     */
    @Resource
   private ProductGateway productGateway;

    /**
     * 支付宝请求网关服务
     */
    @Resource
    private AlipayRequestGateway alipayRequestGateway;

    /**
     * 锁单服务接口; 正常是通过 feign 或者 restTemplate 调用拼团预购营销服务
     * 现在是单体项目，直接调用锁单服务
     */
    @Resource
    private IMarketTradeService marketTradeService;



    /**
     * mock 商品服务，查询固定的SKU商品信息
     * @param productId
     * @return
     */
    @Override
    public ProductEntity queryProductByProductId(String productId) {

        ProductDTO productDTO = productGateway.queryProductByProductId(productId);
        return ProductEntity.builder()
                .productId(productDTO.getProductId())
                .productName(productDTO.getProductName())
                .productDesc(productDTO.getProductDesc())
                .price(productDTO.getPrice())
                .build();
    }

    /**
     * 锁定营销订单，正常是调用营销服务的锁单接口
     * 现在是单体项目，直接调用锁单服务
     * @param lockMarketPayOrderVO 锁单信息对象
     * @return
     */
    @Override
    public GroupMarketProductPriceVO lockMarketPayOrder(LockMarketPayOrderVO lockMarketPayOrderVO) {
        LockMarketPayOrderRequestDTO requestDTO = LockMarketPayOrderRequestDTO.builder()
                .userId(lockMarketPayOrderVO.getUserId())
                .activityId(lockMarketPayOrderVO.getActivityId())
                .goodsId(lockMarketPayOrderVO.getProductId())
                .teamId(lockMarketPayOrderVO.getTeamId())
                .outTradeNo(lockMarketPayOrderVO.getOrderId())
                .channel(lockMarketPayOrderVO.getChannel())
                .source(lockMarketPayOrderVO.getSource())
                .build();

        requestDTO.setNotifyUrl(notifyUrl); // 设置成团回调地址
        Response<LockMarketPayOrderResponseDTO> lockedMarketResponse = marketTradeService.lockMarketPayOrder(requestDTO);
        if (ObjectUtils.isEmpty(lockedMarketResponse))
               throw new AppException(ResponseCode.UN_ERROR,"营销服务锁单失败");
        if (!"E0000".equals(lockedMarketResponse.getCode())) {
            log.error("=======营销服务锁单响应结果异常:code:{}，info:{}=========", lockedMarketResponse.getCode(), lockedMarketResponse.getInfo());
            throw new AppException(ResponseCode.UN_ERROR.getCode(),lockedMarketResponse.getInfo());
        }
        LockMarketPayOrderResponseDTO responseData = lockedMarketResponse.getData();
        GroupMarketProductPriceVO productPriceVO = GroupMarketProductPriceVO.builder()
                .originalPrice(responseData.getOriginalPrice())
                .deductionPrice(responseData.getDeductionPrice())
                .payPrice(responseData.getPayPrice())
                .teamId(responseData.getTeamId()).build();
        log.info("=======营销服务锁单响应结果:originalPrice:{}，deductionPrice: {}, payPrice: {} =========",
                productPriceVO.getOriginalPrice(),productPriceVO.getDeductionPrice(),productPriceVO.getPayPrice());
        return productPriceVO;
    }

    @Override
    public void settlementMarketPayOrder(SettlementMarketPayOrderVO settlementVo) {
        SettlementMarketPayOrderRequestDTO requestDTO = new SettlementMarketPayOrderRequestDTO();
        requestDTO.setSource(settlementVo.getSource());
        requestDTO.setChannel(settlementVo.getChannel());
        requestDTO.setUserId(settlementVo.getUserId());
        requestDTO.setOutTradeNo(settlementVo.getOrderId());
        requestDTO.setOutTradeTime(settlementVo.getTradeTime());
        Response<SettlementMarketPayOrderResponseDTO> settlementReq = marketTradeService.settleMarketPayOrder(requestDTO);
        if (ObjectUtils.isEmpty(settlementReq))
            return;
        if (!"E0000".equals(settlementReq.getCode())) {
            log.error("=======营销服务结算响应结果异常:code:{}，info:{}=========", settlementReq.getCode(), settlementReq.getInfo());
            throw new AppException(ResponseCode.UN_ERROR.getCode(),"营销服务锁单失败");
        }
    }

    @Override
    public String createAlipayPagePayOrder(String orderId, BigDecimal payAmount, String productName,String source, String channel) {
        return alipayRequestGateway.createPagePayOrder(orderId, payAmount, productName, source, channel);
    }

}
