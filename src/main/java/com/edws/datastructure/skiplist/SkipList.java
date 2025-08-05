package com.edws.datastructure.skiplist;

import java.util.Random;

/**
 * @author whs
 */
public class SkipList<K extends Comparable<K>, V> {

    // 跳表最高层级
    private static final int MAX_LEVEL = 32;

    // 当前跳表最高层级
    private int currentMaxLevel;

    // 跳表头节点，由该节点串联起每一层的第一个节点
    private final SkipListNode<K, V> header;

    // 随机层级升级概率，论文建议0.25
    private final double p = 0.25;

    private final Random random;

    public SkipList() {
        random = new Random();
        currentMaxLevel = 1;
        // 头节点不存数据
        header = new SkipListNode<>(null, null, MAX_LEVEL);
    }

    // 跟据指定key查找value
    public V search(K key) {
        // 从头节点最高层开始查询
        SkipListNode<K, V> previous = header;
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            // 如果该层级下一个节点不小于参数key, 则该节点为当前层级的前置节点
            // 最终结果previous节点是第一层的查询结果的前置节点
            while (previous.forwardArray[i] != null
                && previous.forwardArray[i].getKey().compareTo(key) < 0) {
                previous = previous.forwardArray[i];
            }
        }
        // 判断前置节点的后一个节点的key是否与参数key相等，不相等说明不存在查询节点
        SkipListNode<K, V> resultNode = previous.forwardArray[0];
        if (resultNode == null || resultNode.getKey().compareTo(key) != 0) {
            return null;
        }
        return resultNode.getValue();
    }

    // 获取新节点level
    private Integer getNewLevel() {
        int level = 1;
        while (random.nextDouble() < p && level <= MAX_LEVEL) {
            level++;
        }
        return level;
    }

    // 插入数据
    public void put(K key, V value) {
        // previousArray数组记录每一层的前置节点
        SkipListNode<K, V>[] previousArray = new SkipListNode[MAX_LEVEL];
        SkipListNode<K, V> previous = header;
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            while (previous.forwardArray[i] != null
                && previous.forwardArray[i].getKey().compareTo(key) < 0) {
                previous = previous.forwardArray[i];
            }
            previousArray[i] = previous;
        }
        // 判断是否已存在，已存在直接更新
        SkipListNode<K, V> current = previous.forwardArray[0];
        if (current != null && current.getKey().compareTo(key) == 0) {
            current.setValue(value);
            return;
        }
        // 新节点层级
        Integer newLevel = this.getNewLevel();
        if (newLevel > currentMaxLevel) {
            // 如果新节点比原maxLevel大，为新层级设置前置节点为header
            for (int i = currentMaxLevel; i < newLevel; i++) {
                previousArray[i] = header;
            }
            // 更新最大节点
            currentMaxLevel = newLevel;

        }
        // 创建新节点
        SkipListNode<K, V> newNode = new SkipListNode<>(key, value, newLevel);
        // 更新每一层前置节点和后置节点的引用
        for (int i = newLevel - 1; i >= 0; i--) {
            SkipListNode<K, V> previousNode = previousArray[i];
            SkipListNode<K, V> forwardNode = previousNode.forwardArray[i];
            previousNode.forwardArray[i] = newNode;
            newNode.forwardArray[i] = forwardNode;
        }
    }

    public V delete(K key) {
        // previousArray数组记录每一层的前置节点
        SkipListNode<K, V>[] previousArray = new SkipListNode[currentMaxLevel];
        SkipListNode<K, V> previous = header;
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            while (previous.forwardArray[i] != null
                && previous.forwardArray[i].getKey().compareTo(key) < 0) {
                previous = previous.forwardArray[i];
            }
            previousArray[i] = previous;
        }
        V v = null;
        // 最底层前置节点的后一个节点为需要删除的节点
        SkipListNode<K, V> needToDelete = previous.forwardArray[0];
        // 如果需要删除的节点为空或key与参数不相等证明不存在该节点，返回null
        if (needToDelete == null || needToDelete.getKey().compareTo(key) != 0) {
            return null;
        }
        // 修改每一层前置节点和后置节点的引用
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            SkipListNode<K, V> forward = previousArray[i].forwardArray[i];
            if (forward != null && forward == needToDelete) {
                previousArray[i].forwardArray[i] = forward.forwardArray[i];
                if (i == 0) {
                    v = forward.getValue();
                }
            }
        }
        // 判断是否需要修改最高层级
        int newLevel = currentMaxLevel;
        while (newLevel > 1 && header.forwardArray[newLevel - 1] == null) {
            newLevel -= 1;
        }
        currentMaxLevel = newLevel;
        return v;
    }

    public void print() {
        System.out.println("该跳表中层数：" + currentMaxLevel);
        for (int i = currentMaxLevel - 1; i >= 0; i--) {
            System.out.print("Level " + (i + 1) + ": ");
            SkipListNode<K, V> currentNode = this.header.forwardArray[i];
            while (currentNode != null) {
                System.out.print(currentNode + " -> ");
                currentNode = currentNode.forwardArray[i];
            }
            System.out.println("NIL");
        }
    }
}
