package cn.hjw.dev.platform.types.sdk.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信消息文本实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XStreamAlias("xml")
public class MessageTextEntity {

    /**
     * 接收方微信号
     */
    @XStreamAlias("ToUserName")
    private String toUserName;

    /**
     * 发送方微信号
     */
    @XStreamAlias("FromUserName")
    private String fromUserName;

    /**
     * 消息创建时间 （整型）
     */
    @XStreamAlias("CreateTime")
    private String createTime;

    /**
     * 消息类型
     */
    @XStreamAlias("MsgType")
    private String msgType;

    /**
     * 事件类型
     */
    @XStreamAlias("Event")
    private String event;

    /**
     * 事件KEY值
     */
    @XStreamAlias("EventKey")
    private String eventKey;

    /**
     * 消息id，64位整型
     */
    @XStreamAlias("MsgID")
    private String msgId;

    /**
     * 消息状态
     */
    @XStreamAlias("Status")
    private String status;

    /**
     * 二维码的ticket，可用来换取二维码图片
     */
    @XStreamAlias("Ticket")
    private String ticket;

    /**
     * 文本消息内容
     */
    @XStreamAlias("Content")
    private String content;
}