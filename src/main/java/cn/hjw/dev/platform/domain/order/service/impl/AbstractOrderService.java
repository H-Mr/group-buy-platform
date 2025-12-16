package cn.hjw.dev.platform.domain.order.service.impl;

import cn.hjw.dev.platform.domain.order.adapter.port.IOrderPort;
import cn.hjw.dev.platform.domain.order.adapter.repository.IOrderRepository;
import cn.hjw.dev.platform.domain.order.model.aggregate.CreateOrderAggregate;
import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ProductEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.GroupMarketProductPriceVO;
import cn.hjw.dev.platform.domain.order.model.valobj.LockMarketPayOrderVO;
import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.service.IOrderService;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;

/**
 * 抽象订单服务
 * 做两件事：
 * 1. 查询是否有未支付订单，有则返回； 查询是否有待创建支付订单的订单，有则补全支付订单，返回；
 * 2. 正常创建订单流程： 创建订单基础数据，保存本地订单，营销锁单，创建预支付订单（补全支付url）
 * @author hjw
 */
@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository repository;

    protected final IOrderPort port;

    /**
     * 构造函数注入
     * @param repository
     * @param port
     */
    public AbstractOrderService(IOrderRepository repository, IOrderPort port) {
        this.repository = repository;
        this.port = port;
    }

    /**
     * 模板方法模式+ 策略模式
     * @param shopCartEntity
     * @return
     * @throws Exception
     */
    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception {
        // 1. 查询当前用户是否存在掉单和未支付订单
        OrderEntity unpaidOrderEntity = repository.queryUnPayOrder(shopCartEntity);
        // 判断：1. 存在未支付订单 2. 存在已创建订单，但是没有支付地址
        if (null != unpaidOrderEntity && OrderStatusVO.PAY_WAIT.equals(unpaidOrderEntity.getOrderStatusVO())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}", shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrderEntity.getOrderId());
            return PayOrderEntity.builder()
                    .orderId(unpaidOrderEntity.getOrderId())
                    .payUrl(unpaidOrderEntity.getPayUrl())
                    .build();
        } else if (null != unpaidOrderEntity && OrderStatusVO.CREATE.equals(unpaidOrderEntity.getOrderStatusVO())) {
            // 已创建订单，但是没有支付地址，补全支付地址
            // todo repository 获取未支付订缺字段
            log.info("创建订单-掉单，主订单创建，支付订单没有创建，orderId: {}", unpaidOrderEntity.getOrderId());
            BigDecimal payAmount = unpaidOrderEntity.getPayAmount();
            BigDecimal deductionAmount = unpaidOrderEntity.getMarketDeductionAmount();

            if(MarketTypeVO.GROUP_BUY_MARKET.equals(unpaidOrderEntity.getMarketType()) && deductionAmount.compareTo(BigDecimal.ZERO) == 0) {
                // 有营销,没有优惠金额，锁定营销优惠
                BigDecimal[] deductionAmountAndPayAmount = this.lockMarketPayOrder(unpaidOrderEntity, shopCartEntity);
                payAmount = deductionAmountAndPayAmount[0];
                deductionAmount = deductionAmountAndPayAmount[1];
            }
            // 创建预支付订单
            return this.doPrepayOrder(
                    shopCartEntity.getUserId(),
                    unpaidOrderEntity.getProductId(),
                    unpaidOrderEntity.getProductName(),
                    unpaidOrderEntity.getOrderId(),
                    deductionAmount,
                    payAmount);
        }
        // 正常流程创建新主订单

        // 1. 调用商品服务，查询商品信息 mock(从sku表获取指定的商品信息)
        ProductEntity productEntity = port.queryProductByProductId(shopCartEntity.getProductId());

        // 2. 创建订单基础数据
        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(productEntity, shopCartEntity);

        // 应该没必要了？
        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(shopCartEntity.getUserId()) // 下单用户
                .productEntity(productEntity) // 商品实体
                .orderEntity(orderEntity) // 基本订单实体
                .build();

        // 3. 创建本地订单，这是抽象方法，由子类实现创建订单的细节
        this.doSaveOrder(orderAggregate);

        // 4. 营销优惠锁单
        BigDecimal payAmount = orderEntity.getPayAmount();
        BigDecimal deductionAmount = orderEntity.getMarketDeductionAmount();
        if (MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(shopCartEntity.getMarketTypeVO().getCode())) {
            BigDecimal[] deductionAmountAndPayAmount = this.lockMarketPayOrder(orderEntity, shopCartEntity);
            payAmount = deductionAmountAndPayAmount[0];
            deductionAmount = deductionAmountAndPayAmount[1];
        }
        // 5. 创建预支付订单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(
                shopCartEntity.getUserId(),
                productEntity.getProductId(),
                productEntity.getProductName(),
                orderEntity.getOrderId(),
                deductionAmount,
                payAmount);

        log.info("创建订单-完成，生成支付单。userId: {} orderId: {} payUrl: {}", shopCartEntity.getUserId(), orderEntity.getOrderId(), payOrderEntity.getPayUrl());

        return payOrderEntity;
    }

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

    /**
     * 创建支付订单，营销锁单,更新订单的支付金额，优惠金额，支付时间，支付地址
     * @param userId 用户ID
     * @param productId 商品ID
     * @param productName 商品名称
     * @param orderId 订单ID
     * @param deductionAmount 优惠金额
     * @param payAmount 支付金额
     * @return 支付订单实体
     * @throws AlipayApiException 支付宝API异常
     */
    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String orderId,BigDecimal deductionAmount, BigDecimal payAmount) throws AlipayApiException;

    /**
     * 通过端口调用锁单服务进行锁单计算优惠额度
     * @param orderEntity 锁单的订单
     * @param shopCartEntity 购物车信息
     * @return 返回支付金额和优惠金额
     */
    private BigDecimal[] lockMarketPayOrder(OrderEntity orderEntity, ShopCartEntity shopCartEntity) {
        LockMarketPayOrderVO lockMarketPayOrderVO = LockMarketPayOrderVO.builder()
                .userId(shopCartEntity.getUserId())
                .activityId(shopCartEntity.getActivityId())
                .teamId(shopCartEntity.getTeamId())
                .orderId(orderEntity.getOrderId())
                .productId(shopCartEntity.getProductId())
                .build();
        GroupMarketProductPriceVO groupMarketProductPriceVO = this.port.lockMarketPayOrder(lockMarketPayOrderVO);

        return new BigDecimal[] {groupMarketProductPriceVO.getPayPrice(),groupMarketProductPriceVO.getDeductionPrice()};
    }
}
