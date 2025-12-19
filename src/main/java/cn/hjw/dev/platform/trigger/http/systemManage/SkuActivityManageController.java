package cn.hjw.dev.platform.trigger.http.systemManage;

import cn.hjw.dev.platform.api.dto.admin.SCSkuActivityRequestDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.ISCSkuActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.platform.infrastructure.dao.po.SCSkuActivity;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * SKU-活动 管理控制器
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/sku_activity/")
public class SkuActivityManageController {

    @Resource
    private ISCSkuActivityDao scSkuActivityDao;
    @Resource
    private IGroupBuyActivityDao activityDao;

    /**
     * 查询 渠道-商品-活动 绑定列表
     */
    @GetMapping("list")
    public Response<List<SCSkuActivity>> querySCSkuActivityList() {
        try {
            List<SCSkuActivity> list = scSkuActivityDao.querySCSkuActivityList();
            return Response.<List<SCSkuActivity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("查询绑定列表失败", e);
            return Response.<List<SCSkuActivity>>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    /**
     * 绑定活动与 SKU 关联关系 (核心配置接口)
     */
    @PostMapping("bind")
    public Response<Boolean> bindActivitySku(@RequestBody SCSkuActivityRequestDTO request) {
        try {
            // 1. 基础校验
            if (request.getActivityId() == null || request.getGoodsId() == null) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("参数不完整").build();
            }

            // 2. 校验 Activity 是否存在 (防止绑定了无效活动)
            GroupBuyActivity activity = activityDao.queryGroupBuyActivityByActivityId(request.getActivityId());
            if (activity == null) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("活动ID不存在").build();
            }

            // 3. 构建 PO
            SCSkuActivity po = SCSkuActivity.builder()
                    .source(request.getSource())
                    .channel(request.getChannel())
                    .goodsId(request.getGoodsId())
                    .activityId(request.getActivityId())
                    .build();

            // 4. 插入或更新 (幂等设计)
            // 先查是否存在该商品在该渠道的配置
            SCSkuActivity exist = scSkuActivityDao.querySCSkuActivityBySCGoodsId(po);
            if (exist != null) {
                // 如果存在，更新绑定的活动ID
                scSkuActivityDao.updateActivityId(po);
            } else {
                // 不存在则插入新记录
                scSkuActivityDao.insert(po);
            }
            return Response.<Boolean>builder().code(ResponseCode.SUCCESS.getCode()).data(true).build();
        } catch (Exception e) {
            log.error("绑定活动失败", e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(),"绑定活动失败");
        }
    }
}