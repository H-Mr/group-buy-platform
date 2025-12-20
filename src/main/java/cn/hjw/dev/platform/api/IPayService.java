package cn.hjw.dev.platform.api;

import cn.hjw.dev.platform.api.dto.CreateOrderDTO;
import cn.hjw.dev.platform.api.dto.CreatePayRequestDTO;
import cn.hjw.dev.platform.api.response.Response;

/**
 * 对外接口-支付下单服务服务
 */
public interface IPayService {

    /**
     * 商品下单服务，传入请求参数，构建购物车，创建支付单
     * @param createPayRequestDTO
     * @return
     */
    Response<CreateOrderDTO> createPayOrder(CreatePayRequestDTO createPayRequestDTO) throws Exception;
}
