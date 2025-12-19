package cn.hjw.dev.platform.trigger.http.systemManage;

import cn.hjw.dev.platform.api.dto.admin.ActivityCreateRequestDTO;
import cn.hjw.dev.platform.api.dto.admin.ActivityUpdateRequestDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.infrastructure.dao.ICrowdTagsDao;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.po.CrowdTags;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 拼团活动管理控制器
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/admin/activity/")
public class ActivityManagerController {

    @Resource
    private IGroupBuyActivityDao activityDao;

    @Resource
    private ICrowdTagsDao crowdTagsDao;

    /**
     * 查询活动列表
     */
    @GetMapping("list")
    public Response<List<GroupBuyActivity>> queryActivityList() {
        try {
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

    /**
     * 查询人群标签列表 (创建活动/折扣时的辅助接口)
     */
    @GetMapping("query_tag_list")
    public Response<List<CrowdTags>> queryCrowdTagsList() {
        try {
            List<CrowdTags> list = crowdTagsDao.queryCrowdTagsList();
            return Response.<List<CrowdTags>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("查询标签列表失败", e);
            return Response.<List<CrowdTags>>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }

    /**
     * 创建活动
     */
    @PostMapping("create")
    public Response<Long> createActivity(@RequestBody ActivityCreateRequestDTO request) {
        try {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                return Response.<Long>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("结束时间不能早于开始时间").build();
            }

            // 生成 ActivityId (实际生产建议使用雪花算法)
            Long activityId = Long.parseLong(RandomStringUtils.randomNumeric(6));

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

    /**
     * 更新活动配置
     */
    @PostMapping("update")
    public Response<Boolean> updateActivity(@RequestBody ActivityUpdateRequestDTO request) {
        try {
            if (request.getActivityId() == null) {
                return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("ActivityId不能为空").build();
            }

            if (request.getStartTime() != null && request.getEndTime() != null) {
                if (request.getEndTime().isBefore(request.getStartTime())) {
                    return Response.<Boolean>builder().code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("结束时间不能早于开始时间").build();
                }
            }

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
                    .build();

            int rows = activityDao.update(activity);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(rows > 0)
                    .build();
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return Response.<Boolean>builder().code(ResponseCode.UN_ERROR.getCode()).build();
        }
    }
}