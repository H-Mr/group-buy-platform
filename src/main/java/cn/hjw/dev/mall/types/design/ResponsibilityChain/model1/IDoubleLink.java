package cn.hjw.dev.mall.types.design.ResponsibilityChain.model1;

/**
 * 定义双向链表接口
 * @author hjw
 */
public interface IDoubleLink<T> {

    /**
     * 添加到头节点
     * @param e
     */
    void addFirst(T e);

    /**
     * 添加到尾节点
     * @param e
     */
    void addLast(T e);

    /**
     * 添加节点，默认添加到尾节点
     * @param e
     */
    default void add(T e){
        addLast(e);
    }

    /**
     * 移除节点
     * @param e
     */
    void remove(T e);

    /**
     * 根据索引获取节点
     * @param index
     * @return
     */
    T get(int index);

    int size();


}
