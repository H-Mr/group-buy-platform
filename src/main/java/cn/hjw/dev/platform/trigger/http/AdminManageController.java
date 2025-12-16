package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.dto.admin.*;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.*;
import cn.hjw.dev.platform.infrastructure.dao.po.*;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 后台管理 (token)
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/")
public class AdminManageController {

    @Resource private IGroupBuyActivityDao activityDao;
    @Resource private IGroupBuyDiscountDao discountDao;
    @Resource private ISkuDao skuDao;
    @Resource private ISCSkuActivityDao scSkuActivityDao;
    @Resource private ISkuStockDao skuStockDao;

    // ==========================================
    // 1. 商品 SKU 管理
    // ==========================================

    /**
     * 创建商品 SKU
     * @param request
     * @return
     */
    @PostMapping("create_sku")
    @Transactional(rollbackFor = Exception.class)
    public Response<String> createSku(@RequestBody SkuCreateRequestDTO request) {
        try {
            // 1. 生成 8 位数字作为 goodsId (业务主键)
            String goodsId = RandomStringUtils.randomNumeric(8);

            // 2. 构建 PO
            Sku sku = Sku.builder()
                    .goodsId(goodsId)
                    .goodsName(request.getGoodsName())
                    .originalPrice(request.getOriginalPrice())
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .build();

            // 3. 插入数据库
            skuDao.insert(sku);

            // 4. 初始化库存 (默认0)
            SkuStock stock = SkuStock.builder()
                    .goodsId(goodsId)
                    .totalCount(0)
                    .stockCount(0)
                    .build();
            skuStockDao.insert(stock);

            log.info("创建商品成功 goodsId:{}", goodsId);
            return Response.<String>builder().code(ResponseCode.SUCCESS.getCode()).info(ResponseCode.SUCCESS.getInfo()).data(goodsId).build();
        } catch (Exception e) {
            log.error("创建商品失败", e);
            throw new RuntimeException("创建商品事务回滚");
        }
    }

    // ==========================================
    // 2. 活动配置管理
    // ==========================================

    /**
     * 创建活动
     * @param request
     * @return
     */
    @PostMapping("create_activity")
    public Response<Long> createActivity(@RequestBody ActivityCreateRequestDTO request) {
        try {
            // 1. 生成 ActivityId (使用随机8位数字，转Long)
            // 实际生产建议用 Snowflake 算法
            Long activityId = Long.parseLong(RandomStringUtils.randomNumeric(6));

            // 2. 参数校验
            if (request.getEndTime().isBefore(request.getStartTime())) {
                return Response.<Long>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("结束时间不能早于开始时间").build();
            }

            // 3. 构建 PO
            GroupBuyActivity activity = GroupBuyActivity.builder()
                    .activityId(activityId)
                    .activityName(request.getActivityName())
                    .discountId(request.getDiscountId())
                    .groupType(request.getGroupType())
                    .takeLimitCount(request.getTakeLimitCount())
                    .target(request.getTarget())
                    .validTime(request.getValidTime())
                    .status(request.getStatus())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .tagId(request.getTagId())
                    .tagScope(request.getTagScope())
                    .build();

            activityDao.insert(activity);

            log.info("创建活动成功 activityId:{}", activityId);
            return Response.<Long>builder().code(ResponseCode.SUCCESS.getCode()).data(activityId).build();
        } catch (Exception e) {
            log.error("创建活动失败", e);
            return Response.<Long>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    // ==========================================
    // 3. 折扣配置管理
    // ==========================================

    /**
     * 创建折扣
     * @param request
     * @return
     */
    @PostMapping("create_discount")
    public Response<Long> createDiscount(@RequestBody DiscountCreateRequestDTO request) {
        try {
            // 1. 生成 DiscountId
            Long discountId = Long.parseLong(RandomStringUtils.randomNumeric(8));

            // 2. 构建 PO
            GroupBuyDiscount discount = GroupBuyDiscount.builder()
                    .discountId(discountId)
                    .discountName(request.getDiscountName())
                    .discountDesc(request.getDiscountDesc())
                    .discountType(request.getDiscountType())
                    .marketPlan(request.getMarketPlan())
                    .marketExpr(request.getMarketExpr())
                    .tagId(request.getTagId())
                    .build();

            discountDao.insertGroupBuyActivityDiscount(discount);

            return Response.<Long>builder().code(ResponseCode.SUCCESS.getCode()).data(discountId).build();
        } catch (Exception e) {
            log.error("创建折扣失败", e);
            return Response.<Long>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    // ==========================================
    // 4. 营销配置绑定
    // ==========================================

    /**
     * 绑定活动与 SKU 关联关系
     * @param request
     * @return
     */
    @PostMapping("bind_activity_sku")
    public Response<Boolean> bindActivitySku(@RequestBody SCSkuActivityRequestDTO request) {
        try {
            // 1. 构建 PO (注意：这里没有业务ID生成，因为它是关联表，依赖已有的 businessId)
            SCSkuActivity po = SCSkuActivity.builder()
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .goodsId(request.getGoodsId())
                    .activityId(request.getActivityId())
                    .build();

            // 2. 校验 Activity 是否存在
            GroupBuyActivity activity = activityDao.queryValidGroupBuyActivityId(request.getActivityId());
            if (activity == null) {
                // 如果查不到有效的，再试查一下所有的（可能活动还没生效，但允许配置）
                GroupBuyActivity anyActivity = activityDao.queryGroupBuyActivityByActivityId(request.getActivityId());
                if (anyActivity == null) {
                    return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("活动ID不存在").build();
                }
            }

            // 3. 插入或更新
            SCSkuActivity exist = scSkuActivityDao.querySCSkuActivityBySCGoodsId(po);
            if (exist != null) {
                scSkuActivityDao.updateActivityId(po);
            } else {
                scSkuActivityDao.insert(po);
            }
            return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
        } catch (Exception e) {
            log.error("绑定活动失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    // ==========================================
    // 5. 更新 SKU 信息
    // ==========================================
    /**
     * 更新 SKU 信息
     * @param request
     * @return
     */
    @PostMapping("update_sku")
    public Response<Boolean> updateSku(@RequestBody SkuUpdateRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getGoodsId())) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("GoodsId不能为空").build();
            }

            // 构建 PO：只设置允许更新的字段
            Sku sku = Sku.builder()
                    .goodsId(request.getGoodsId())
                    .goodsName(request.getGoodsName())
                    .originalPrice(request.getOriginalPrice())
                    // source, channel 不允许更新，id 不设置，updateTime 由 SQL 处理
                    .build();

            int rows = skuDao.update(sku);
            if (rows > 0) {
                log.info("更新商品成功 goodsId:{}", request.getGoodsId());
                return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
            } else {
                return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info("商品不存在或未变更").build();
            }
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info("更新失败").build();
        }
    }

    // ==========================================
    // 6. 增加库存管理
    // ==========================================
    /**
     * 增加库存
     * @param request
     * @return
     */
    @PostMapping("add_stock")
    public Response<Boolean> addStock(@RequestBody StockAddRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getGoodsId()) || request.getCount() == null || request.getCount() <= 0) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("参数错误").build();
            }

            // 直接调用 DAO，注意这里不是 PO，是普通参数
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

    // ==========================================
    // 7. 更新活动配置
    // ==========================================
    /**
     * 更新活动配置
     * @param request
     * @return
     */
    @PostMapping("update_activity")
    public Response<Boolean> updateActivity(@RequestBody ActivityUpdateRequestDTO request) {
        try {
            if (request.getActivityId() == null) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("ActivityId不能为空").build();
            }

            // 业务校验：结束时间不能早于开始时间（如果有传时间的话）
            if (request.getStartTime() != null && request.getEndTime() != null) {
                if (request.getEndTime().isBefore(request.getStartTime())) {
                    return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("结束时间不能早于开始时间").build();
                }
            }

            // 构建 PO
            GroupBuyActivity activity = GroupBuyActivity.builder()
                    .activityId(request.getActivityId())
                    .activityName(request.getActivityName())
                    .discountId(request.getDiscountId())
                    .groupType(request.getGroupType())
                    .takeLimitCount(request.getTakeLimitCount())
                    .target(request.getTarget())
                    .validTime(request.getValidTime())
                    .status(request.getStatus())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .tagId(request.getTagId())
                    .tagScope(request.getTagScope())
                    // id 不设置，createTime 不设置，updateTime 由 SQL 处理
                    .build();

            int rows = activityDao.update(activity);
            if (rows > 0) {
                log.info("更新活动成功 activityId:{}", request.getActivityId());
                return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
            } else {
                return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info("活动不存在").build();
            }
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    // ==========================================
    // 8. 查询活动列表 (后台专用)
    // ==========================================
    /**
     * 查询活动列表 (后台专用)
     * @return
     */
    @GetMapping("query_activity_list_admin")
    public Response<List<GroupBuyActivity>> queryActivityListAdmin() {
        try {
            // 注意：这里返回 List<PO> 在内部管理系统中通常是可以接受的
            // 如果追求极致规范，可以定义一个 ActivityResponseDTO 并进行转换
            List<GroupBuyActivity> list = activityDao.queryGroupBuyActivityList();
            return Response.<List<GroupBuyActivity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("查询活动列表失败", e);
            return Response.<List<GroupBuyActivity>>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }
}