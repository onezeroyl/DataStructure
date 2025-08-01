package com.edws.datastructure.skiplist;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 跳跃表节点
 * @author whs
 */
@Data
public class SkipListNode<T extends Comparable<T>> {

    /**
     * 存储的数据
     */
    private T data;

    /**
     * 分数，用于排序
     */
    private double score;

    /**
     * 层级列表，每一层都有指向下一个节点的指针和跨度信息
     */
    private List<Level<T>> levelList;

    /**
     * 指向前一个节点的指针（用于反向遍历）
     */
    private SkipListNode<T> previous;

    /**
     * 构造函数
     * @param data 数据
     * @param score 分数
     * @param level 层数
     */
    public SkipListNode(T data, double score, int level) {
        this.data = data;
        this.score = score;
        this.levelList = new ArrayList<>(level);
        for (int i = 0; i < level; i++) {
            this.levelList.add(new Level<>());
        }
        this.previous = null;
    }

    /**
     * 无参构造函数（用于头节点）
     */
    public SkipListNode() {
        this.data = null;
        this.score = Double.NEGATIVE_INFINITY;
        this.levelList = new ArrayList<>();
        this.previous = null;
    }
}

/**
 * 跳跃表的层级信息
 */
@Data
class Level<T extends Comparable<T>> {

    /**
     * 跨度：当前节点到下一个节点之间的距离
     */
    private Integer span;

    /**
     * 指向下一个节点的指针
     */
    private SkipListNode<T> forward;

    /**
     * 构造函数
     */
    public Level() {
        this.span = 0;
        this.forward = null;
    }
}
