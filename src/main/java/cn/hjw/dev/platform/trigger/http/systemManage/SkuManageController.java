package cn.hjw.dev.platform.trigger.http.systemManage;

import cn.hjw.dev.platform.api.dto.admin.SkuCreateRequestDTO;
import cn.hjw.dev.platform.api.dto.admin.SkuUpdateRequestDTO;
import cn.hjw.dev.platform.api.dto.admin.StockAddRequestDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.ISkuDao;
import cn.hjw.dev.platform.infrastructure.dao.ISkuStockDao;
import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import cn.hjw.dev.platform.infrastructure.dao.po.SkuStock;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * SKU 管理控制器
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/sku/")
public class SkuManageController {

    @Resource
    private ISkuDao skuDao;
    @Resource
    private ISkuStockDao skuStockDao;

    /**
     * 查询商品列表
     */
    @GetMapping("list")
    public Response<List<Sku>> querySkuList() {
        try {
            List<Sku> list = skuDao.querySkuList();
            return Response.<List<Sku>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return Response.<List<Sku>>builder().code(ResponseCode.UN_ERROR.getCode()).info("系统异常").build();
        }
    }

    /**
     * 创建商品 (自动初始化库存)
     */
    @PostMapping("create")
    @Transactional(rollbackFor = Exception.class)
    public Response<String> createSku(@RequestBody SkuCreateRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getGoodsName())) {
                return Response.<String>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("商品名称不能为空").build();
            }
            // 1. 生成 8 位数字作为 goodsId
            String goodsId = RandomStringUtils.randomNumeric(8);

            // 2. 构建 SKU PO
            Sku sku = Sku.builder()
                    .goodsId(goodsId)
                    .goodsName(request.getGoodsName())
                    .originalPrice(request.getOriginalPrice())
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .build();
            skuDao.insert(sku);

            // 3. 初始化库存 (默认0)
            SkuStock stock = SkuStock.builder()
                    .goodsId(goodsId)
                    .totalCount(0)
                    .stockCount(0)
                    .build();
            skuStockDao.insert(stock);

            log.info("创建商品成功 goodsId:{}", goodsId);
            return Response.<String>builder().code(ResponseCode.SUCCESS.getCode()).data(goodsId).build();
        } catch (Exception e) {
            log.error("创建商品失败", e);
            throw new RuntimeException("创建商品事务回滚");
        }
    }

    /**
     * 更新商品信息
     */
    @PostMapping("update")
    public Response<Boolean> updateSku(@RequestBody SkuUpdateRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getGoodsId())) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("GoodsId不能为空").build();
            }

            Sku sku = Sku.builder()
                    .goodsId(request.getGoodsId())
                    .goodsName(request.getGoodsName())
                    .originalPrice(request.getOriginalPrice())
                    .build();

            int rows = skuDao.update(sku);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(rows > 0)
                    .build();
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    /**
     * 增加库存
     */
    @PostMapping("add_stock")
    public Response<Boolean> addStock(@RequestBody StockAddRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getGoodsId()) || request.getCount() == null || request.getCount() <= 0) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("参数错误").build();
            }

            int rows = skuStockDao.increaseStock(request.getGoodsId(), request.getCount());
            if (rows > 0) {
                log.info("库存增加成功 goodsId:{}, count:{}", request.getGoodsId(), request.getCount());
                return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
            } else {
                return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info("商品库存记录不存在").build();
            }
        } catch (Exception e) {
            log.error("增加库存失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }
}