package com.edws.datastructure.skiplist;

/**
 * 跳跃表演示类
 *
 * @author whs
 */
public class SkipListDemo {

    public static void main(String[] args) {
        // 创建一个存储字符串的跳跃表
        SkipList<Integer, String> skipList = new SkipList<>();
        System.out.println("=== 跳跃表演示 ===\n");

        // 测试插入操作
        System.out.println("1. 插入数据:");
        skipList.put(90, "Alice");
        skipList.put(85, "Bob");
        skipList.put(95, "Charlie");
        skipList.put(88, "David");
        skipList.put(92, "Eve");
        skipList.put(87, "Frank");
        skipList.put(93, "Grace");
        skipList.put(95, "Turing");
        System.out.println("插入了7个学生的成绩后跳跃表结构如下：");
        skipList.print();

        System.out.println("2. 查询数据:");
        String name = skipList.search(95);
        System.out.println("查询成绩为95的姓名为：" + name);

        System.out.println("3. 删除数据:");
        String deleteValue = skipList.delete(95);
        System.out.println("删除成绩为95的姓名为" + deleteValue + "后跳跃表结构如下：");
        skipList.print();
    }
} 