package cn.hjw.dev.mall.types.design.ResponsibilityChain.model1;

public class DoubleLink<T> implements IDoubleLink<T>{

    protected final String name;

    private transient int size;

    protected Node<T> first;

    protected Node<T> last;


    public DoubleLink(String name) {
        this.name = name;
        this.first = new Node<>(); // 哨兵节点
        this.last = new Node<>(); // 哨兵节点
        first.next = last;
        last.prev = first;
        this.size = 0;
    }

    /**
     * 添加到头节点
     * @param e
     */
    @Override
    public void addFirst(T e) {
        Node<T> tNode = new Node<>(e);
        tNode.next = first.next;
        first.next.prev = tNode;
        first.next = tNode;
        tNode.prev = first;
        size++;
    }

    @Override
    public void addLast(T e) {
        Node<T> tNode = new Node<>(e);
        tNode.prev = last.prev;
        last.prev.next = tNode;
        tNode.next = last;
        last.prev = tNode;
        size++;
    }

    @Override
    public void remove(T e) {
        Node<T> delNode = this.getNode(e);
        if (delNode != null) {
            delNode.prev.next = delNode.next;
            delNode.next.prev = delNode.prev;
            size--;
        }
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node<T> current = first.next;
        int currentIndex = 0;
        while (current != last) {
            if (currentIndex == index) {
                return current.val;
            }
            current = current.next;
            currentIndex++;
        }
        return null;
    }

    @Override
    public int size() {
        return this.size;
    }

    private Node<T> getNode(T e) {
        Node<T> current = first.next;
        while (current != last) {
            if (current.val.equals(e)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public Node<T> getFirstNode() {
        return this.first.next;
    }

    public Node<T> getLastNode() {
        return this.last;
    }

    /**
     * 节点类
     * @param <T>
     */
    public static class Node<T> {
         public T val;
         public Node<T> prev;
         public Node<T> next;

        public Node() {
            this.val = null;
            this.next = null;
            this.prev = null;
        }

        public Node(T val) {
            this.val = val;
        }


    }
}
