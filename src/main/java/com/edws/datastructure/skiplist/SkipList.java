package com.edws.datastructure.skiplist;

import java.util.Random;
import lombok.Getter;

/**
 * 跳跃表实现
 *
 * @param <T> 数据类型，必须实现Comparable接口
 * @author whs
 */
public class SkipList<T extends Comparable<T>> {

    /**
     * 最大层数
     */
    private static final int MAX_LEVEL = 32;

    /**
     * 升层概率
     */
    private static final double P = 0.25;

    /**
     * 头节点
     */
    private SkipListNode<T> header;

    /**
     * 尾节点
     */
    private SkipListNode<T> tail;

    /**
     * 当前最大层数 -- GETTER -- 获取当前最大层数
     */
    @Getter
    private int level;

    /**
     * 节点数量 -- GETTER -- 获取跳跃表长度
     */
    @Getter
    private long length;

    /**
     * 随机数生成器
     */
    private final Random random;

    /**
     * 构造函数
     */
    public SkipList() {
        this.level = 1;
        this.length = 0;
        this.random = new Random();

        // 初始化头节点
        this.header = new SkipListNode<>();
        for (int i = 0; i < MAX_LEVEL; i++) {
            this.header.getLevelList().add(new Level<>());
        }

        this.tail = null;
    }

    /**
     * 生成随机层数
     *
     * @return 层数
     */
    private int randomLevel() {
        int level = 1;
        while (random.nextDouble() < P && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }

    /**
     * 插入节点
     *
     * @param score 分数
     * @param data  数据
     * @return 插入的节点
     */
    public SkipListNode<T> insert(double score, T data) {
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL];
        int[] rank = new int[MAX_LEVEL];
        SkipListNode<T> current = header;

        // 从最高层开始查找插入位置
        for (int i = level - 1; i >= 0; i--) {
            rank[i] = (i == level - 1) ? 0 : rank[i + 1];

            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                rank[i] += current.getLevelList().get(i).getSpan();
                current = current.getLevelList().get(i).getForward();
            }
            update[i] = current;
        }

        // 生成新节点的层数
        int newLevel = randomLevel();

        // 如果新层数大于当前最大层数，需要更新header的相关层
        if (newLevel > level) {
            for (int i = level; i < newLevel; i++) {
                rank[i] = 0;
                update[i] = header;
                update[i].getLevelList().get(i).setSpan((int) length);
            }
            level = newLevel;
        }

        // 创建新节点
        SkipListNode<T> newNode = new SkipListNode<>(data, score, newLevel);

        // 更新指针和跨度
        for (int i = 0; i < newLevel; i++) {
            newNode.getLevelList().get(i).setForward(update[i].getLevelList().get(i).getForward());
            update[i].getLevelList().get(i).setForward(newNode);

            // 更新跨度
            newNode.getLevelList().get(i)
                .setSpan(update[i].getLevelList().get(i).getSpan() - (rank[0] - rank[i]));
            update[i].getLevelList().get(i).setSpan(rank[0] - rank[i] + 1);
        }

        // 更新没有新节点的层的跨度
        for (int i = newLevel; i < level; i++) {
            update[i].getLevelList().get(i).setSpan(update[i].getLevelList().get(i).getSpan() + 1);
        }

        // 更新backward指针
        newNode.setPrevious((update[0] == header) ? null : update[0]);
        if (newNode.getLevelList().get(0).getForward() != null) {
            newNode.getLevelList().get(0).getForward().setPrevious(newNode);
        } else {
            tail = newNode;
        }

        length++;
        return newNode;
    }

    /**
     * 删除节点
     *
     * @param score 分数
     * @param data  数据
     * @return 是否删除成功
     */
    public boolean delete(double score, T data) {
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL];
        SkipListNode<T> current = header;

        // 查找要删除的节点
        for (int i = level - 1; i >= 0; i--) {
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                current = current.getLevelList().get(i).getForward();
            }
            update[i] = current;
        }

        current = current.getLevelList().get(0).getForward();

        // 检查是否找到目标节点
        if (current != null && current.getScore() == score && current.getData().equals(data)) {
            deleteNode(current, update);
            return true;
        }
        return false;
    }

    /**
     * 删除指定节点
     *
     * @param node   要删除的节点
     * @param update 更新数组
     */
    private void deleteNode(SkipListNode<T> node, SkipListNode<T>[] update) {
        // 更新指针和跨度
        for (int i = 0; i < level; i++) {
            if (update[i].getLevelList().get(i).getForward() == node) {
                update[i].getLevelList().get(i).setSpan(
                    update[i].getLevelList().get(i).getSpan() + node.getLevelList().get(i).getSpan()
                        - 1);
                update[i].getLevelList().get(i).setForward(node.getLevelList().get(i).getForward());
            } else {
                update[i].getLevelList().get(i)
                    .setSpan(update[i].getLevelList().get(i).getSpan() - 1);
            }
        }

        // 更新backward指针
        if (node.getLevelList().get(0).getForward() != null) {
            node.getLevelList().get(0).getForward().setPrevious(node.getPrevious());
        } else {
            tail = node.getPrevious();
        }

        // 删除空的层
        while (level > 1 && header.getLevelList().get(level - 1).getForward() == null) {
            level--;
        }

        length--;
    }

    /**
     * 查找节点
     *
     * @param score 分数
     * @param data  数据
     * @return 找到的节点，如果不存在返回null
     */
    public SkipListNode<T> search(double score, T data) {
        SkipListNode<T> current = header;

        for (int i = level - 1; i >= 0; i--) {
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                current = current.getLevelList().get(i).getForward();
            }
        }

        current = current.getLevelList().get(0).getForward();

        if (current != null && current.getScore() == score && current.getData().equals(data)) {
            return current;
        }
        return null;
    }

    /**
     * 根据排名获取节点（从1开始）
     *
     * @param rank 排名
     * @return 节点
     */
    public SkipListNode<T> getByRank(long rank) {
        if (rank < 1 || rank > length) {
            return null;
        }

        SkipListNode<T> current = header;
        long traversed = 0;

        for (int i = level - 1; i >= 0; i--) {
            while (current.getLevelList().get(i).getForward() != null
                && (traversed + current.getLevelList().get(i).getSpan()) <= rank) {
                traversed += current.getLevelList().get(i).getSpan();
                current = current.getLevelList().get(i).getForward();
            }

            if (traversed == rank) {
                return current;
            }
        }

        return null;
    }

    /**
     * 获取节点的排名
     *
     * @param score 分数
     * @param data  数据
     * @return 排名（从1开始），如果不存在返回0
     */
    public long getRank(double score, T data) {
        SkipListNode<T> current = header;
        long rank = 0;

        for (int i = level - 1; i >= 0; i--) {
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        <= 0))) {
                rank += current.getLevelList().get(i).getSpan();
                current = current.getLevelList().get(i).getForward();
            }

            // 如果找到了目标节点
            if (current.getData() != null && current.getData().equals(data)
                && current.getScore() == score) {
                return rank;
            }
        }

        return 0;
    }

    /**
     * 判断跳跃表是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return length == 0;
    }

    /**
     * 清空跳跃表
     */
    public void clear() {
        header = new SkipListNode<>();
        for (int i = 0; i < MAX_LEVEL; i++) {
            header.getLevelList().add(new Level<>());
        }
        tail = null;
        level = 1;
        length = 0;
    }

    /**
     * 打印跳跃表结构（用于调试）
     */
    public void print() {
        System.out.println("SkipList [length=" + length + ", level=" + level + "]");
        for (int i = level - 1; i >= 0; i--) {
            System.out.print("Level " + i + ": ");
            SkipListNode<T> current = header.getLevelList().get(i).getForward();
            while (current != null) {
                System.out.print("[" + current.getScore() + "," + current.getData() + "] ");
                current = current.getLevelList().get(i).getForward();
            }
            System.out.println();
        }
        System.out.println();
    }
} 