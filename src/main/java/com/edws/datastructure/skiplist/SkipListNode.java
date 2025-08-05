package com.edws.datastructure.skiplist;

import lombok.Getter;
import lombok.Setter;

/**
 * @author whs
 */
public class SkipListNode<K extends Comparable<K>, V> {

    // 每一层级后置节点数组
    final SkipListNode<K, V>[] forwardArray;

    @Getter
    private final K key;

    @Getter
    @Setter
    private V value;

    public SkipListNode(K k, V v, Integer level) {
        this.key = k;
        this.value = v;
        this.forwardArray = new SkipListNode[level];
    }

    public String toString() {
        return "[ " + this.getKey() + ", " + this.getValue() + " ]";
    }

}
