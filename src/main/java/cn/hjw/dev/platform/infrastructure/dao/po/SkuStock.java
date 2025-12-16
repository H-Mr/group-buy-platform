package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品库存表 POJO
 * （动静分离设计，专注处理库存高频扣减场景）
 *
 * @author 开发者名称
 * @date 2025-12-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkuStock {

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 初始总库存
     */
    private Integer totalCount = 0;

    /**
     * 当前剩余库存
     */
    private Integer stockCount = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}