package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IOrderDao {

    // 插入订单
    void insert(PayOrder payOrder);

    // 查询用户未支付订单
    PayOrder queryUnPayOrder(PayOrder payOrder);

    /**
     * 1. 悲观锁（FOR UPDATE）必须依赖事务
     * MySQL 的行锁（FOR UPDATE）是在事务生命周期内生效的：
     * 如果没有开启事务，SELECT ... FOR UPDATE会被当作 “普通查询” 执行（不会加锁），无法达到 “线程排队” 的效果；
     * 只有在事务中执行SELECT ... FOR UPDATE，才会持有行锁，直到事务提交 / 回滚后释放锁。
     * 最终落库的方法，使用悲观锁
     */
    PayOrder queryPayOrderIdForUpdate(PayOrder payOrder);

    // 根据订单号查询订单
    PayOrder queryPayOrderById(String orderId);

    // 查询未支付待通知订单列表
    List<String> queryNoPayNotifyOrder();

    // 查询超时待关闭订单列表
    List<String> queryTimeoutCloseOrderList();

    // ==================== 通用更新方法 ====================

    /**
     * 通用条件更新订单（双PayOrder传参：更新实体 + 条件实体）
     * @param updatePayOrder 承载更新字段的实体（新状态、支付链接、支付时间等）
     * @param conditionPayOrder 承载查询条件的实体（订单号、旧状态、用户ID等）
     * @return 受影响的行数
     */
    int updatePayOrderByCondition(
            @Param("updatePayOrder") PayOrder updatePayOrder,
            @Param("conditionPayOrder") PayOrder conditionPayOrder
    );

    List<PayOrder> queryUserOrderList(@Param("userId") String userId,
                                      @Param("lastId") Long lastId,
                                      @Param("pageSize") Integer pageSize);


    PayOrder queryOrderByUserIdAndOrderId(@Param("userId") String userId,
                                          @Param("orderId") String orderId);





}
