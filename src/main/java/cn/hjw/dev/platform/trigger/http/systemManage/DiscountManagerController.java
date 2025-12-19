package cn.hjw.dev.platform.trigger.http.systemManage;

import cn.hjw.dev.platform.api.dto.admin.DiscountCreateRequestDTO;
import cn.hjw.dev.platform.api.dto.admin.DiscountUpdateRequestDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyDiscountDao;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyDiscount;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 折扣管理控制器
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/discount/")
public class DiscountManagerController {

    @Resource
    private IGroupBuyDiscountDao discountDao;

    /**
     * 查询折扣列表
     */
    @GetMapping("list")
    public Response<List<GroupBuyDiscount>> queryDiscountList() {
        try {
            List<GroupBuyDiscount> list = discountDao.queryGroupBuyDiscountList();
            return Response.<List<GroupBuyDiscount>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("查询折扣列表失败", e);
            return Response.<List<GroupBuyDiscount>>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    /**
     * 创建折扣
     */
    @PostMapping("create")
    public Response<Long> createDiscount(@RequestBody DiscountCreateRequestDTO request) {
        try {
            // 生成 DiscountId
            Long discountId = Long.parseLong(RandomStringUtils.randomNumeric(8));

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

    /**
     * 更新折扣配置
     */
    @PostMapping("update")
    public Response<Boolean> updateDiscount(@RequestBody DiscountUpdateRequestDTO request) {
        try {
            if (StringUtils.isBlank(request.getDiscountId())) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("DiscountId不能为空").build();
            }

            GroupBuyDiscount discount = GroupBuyDiscount.builder()
                    .discountId(Long.valueOf(request.getDiscountId()))
                    .discountName(request.getDiscountName())
                    .discountDesc(request.getDiscountDesc())
                    .discountType(request.getDiscountType())
                    .marketPlan(request.getMarketPlan())
                    .marketExpr(request.getMarketExpr())
                    .tagId(request.getTagId())
                    .build();

            int rows = discountDao.updateGroupBuyActivityDiscount(discount);
            if (rows > 0) {
                return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
            } else {
                return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).info("折扣配置不存在").build();
            }
        } catch (Exception e) {
            log.error("更新折扣失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }
}