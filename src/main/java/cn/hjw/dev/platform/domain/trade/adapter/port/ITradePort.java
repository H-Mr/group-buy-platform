package cn.hjw.dev.platform.domain.trade.adapter.port;

import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 交易接口服务接口
 * @create 2025-01-31 10:38
 */
public interface ITradePort {

    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;

}
