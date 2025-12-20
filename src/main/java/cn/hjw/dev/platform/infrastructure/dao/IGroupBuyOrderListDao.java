package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 用户拼单明细
 * @create 2025-01-11 09:07
 */
@Mapper
public interface IGroupBuyOrderListDao {

    /**
     * 插入用户拼单明细
     * @param groupBuyOrderListReq
     */
    void insert(GroupBuyOrderList groupBuyOrderListReq);

    /**
     * 根据外部交易单号查询用户拼单明细
     * @param groupBuyOrderListReq
     * @return
     */
    GroupBuyOrderList queryGroupBuyOrderRecordByOutTradeNo(GroupBuyOrderList groupBuyOrderListReq);

    /**
     * 根据活动ID查询拼单订单数量
     * @param groupBuyOrderListReq
     * @return
     */
    Integer queryOrderCountByActivityId(GroupBuyOrderList groupBuyOrderListReq);

    List<String> queryGroupBuyCompleteOrderOutTradeNoListByTeamId(String teamId);

    int updateOrderStatus2COMPLETE(GroupBuyOrderList groupBuyOrderListReq);

    List<String> queryGroupBuyCompleteOrderUserIdListByTeamId(String teamId);

    List<String> queryGoodsIdByUserIdAndTeamId(@Param("userId") String userId, @Param("teamId") String teamId);

    String queryTeamIdByOutTradeNo(String outTradeNo);
}
