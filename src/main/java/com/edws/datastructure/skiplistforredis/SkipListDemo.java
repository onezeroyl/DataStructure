package com.edws.datastructure.skiplistforredis;

/**
 * 跳跃表演示类
 * @author whs
 */
public class SkipListDemo {

    public static void main(String[] args) {
        // 创建一个存储字符串的跳跃表
        SkipList<String> skipList = new SkipList<>();

        System.out.println("=== 跳跃表演示 ===\n");

        // 测试插入操作
        System.out.println("1. 插入数据:");
        skipList.insert(90, "Alice");
        skipList.print();
        skipList.insert(85, "Bob");
        skipList.print();
        skipList.insert(95, "Charlie");
        skipList.print();
        skipList.insert(88, "David");
        skipList.print();
        skipList.insert(92, "Eve");
        skipList.print();
        skipList.insert(87, "Frank");
        skipList.insert(93, "Grace");

        System.out.println("插入了7个学生的成绩");
        skipList.print();

        // 测试查找操作
        System.out.println("2. 查找操作:");
        SkipListNode<String> found = skipList.search(92, "Eve");
        if (found != null) {
            System.out.println("找到了 Eve，分数: " + found.getScore());
        } else {
            System.out.println("未找到 Eve");
        }

        found = skipList.search(80, "John");
        if (found != null) {
            System.out.println("找到了 John，分数: " + found.getScore());
        } else {
            System.out.println("未找到 John");
        }
        System.out.println();

        // 测试排名操作
        System.out.println("3. 排名操作:");
        long rank = skipList.getRank(92, "Eve");
        System.out.println("Eve 的排名: " + rank);

        SkipListNode<String> rankNode = skipList.getByRank(1);
        if (rankNode != null) {
            System.out.println("第1名: " + rankNode.getData() + " (分数: " + rankNode.getScore() + ")");
        }

        rankNode = skipList.getByRank(3);
        if (rankNode != null) {
            System.out.println("第3名: " + rankNode.getData() + " (分数: " + rankNode.getScore() + ")");
        }
        System.out.println();

        // 测试删除操作
        System.out.println("4. 删除操作:");
        boolean deleted = skipList.delete(88, "David");
        System.out.println("删除 David: " + (deleted ? "成功" : "失败"));
        skipList.print();

        // 测试状态信息
        System.out.println("5. 跳跃表状态:");
        System.out.println("长度: " + skipList.getLength());
        System.out.println("最大层数: " + skipList.getLevel());
        System.out.println("是否为空: " + skipList.isEmpty());
        System.out.println();

        // 性能测试
        performanceTest();
    }

    /**
     * 性能测试
     */
    private static void performanceTest() {
        System.out.println("=== 性能测试 ===\n");

        SkipList<Integer> skipList = new SkipList<>();
        int testSize = 10000;

        // 插入性能测试
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < testSize; i++) {
            skipList.insert(Math.random() * 10000, i);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("插入 " + testSize + " 个元素耗时: " + (endTime - startTime) + "ms");

        // 查找性能测试
        startTime = System.currentTimeMillis();
        int foundCount = 0;
        for (int i = 0; i < 1000; i++) {
            int searchValue = (int)(Math.random() * testSize);
            if (skipList.search(searchValue, searchValue) != null) {
                foundCount++;
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("查找 1000 次，找到 " + foundCount + " 个，耗时: " + (endTime - startTime) + "ms");

        System.out.println("最终跳跃表长度: " + skipList.getLength());
        System.out.println("最终跳跃表层数: " + skipList.getLevel());
    }
} 