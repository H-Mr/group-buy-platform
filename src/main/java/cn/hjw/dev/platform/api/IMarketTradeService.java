package cn.hjw.dev.platform.api;

import cn.hjw.dev.platform.api.dto.LockMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.LockMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.hjw.dev.platform.api.dto.SettlementMarketPayOrderResponseDTO;
import cn.hjw.dev.platform.api.response.Response;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 营销交易服务接口
 * @create 2025-01-11 13:49
 */
public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

    Response<SettlementMarketPayOrderResponseDTO> settleMarketPayOrder(SettlementMarketPayOrderRequestDTO settlementMarketPayOrderRequestDTO);

}
