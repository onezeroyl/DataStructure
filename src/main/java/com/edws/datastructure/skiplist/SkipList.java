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
     * 总结：初始化跳跃表的基本结构，包括头节点、层级信息等
     * 创建一个空的跳跃表，设置初始状态并准备接受元素插入
     */
    public SkipList() {
        // level - 当前跳跃表的最大层数，初始化为1层（第0层）
        this.level = 1;

        // length - 跳跃表中实际数据节点的数量，初始为空所以是0
        this.length = 0;

        // random - 随机数生成器，用于决定新插入节点的层数
        this.random = new Random();

        // 初始化头节点 - header是一个特殊的哨兵节点，不存储实际数据
        // 它的作用是简化边界条件处理，避免空指针异常
        this.header = new SkipListNode<>();

        // 为头节点的每一层都创建Level对象
        // MAX_LEVEL是最大可能层数，这里预先为所有可能的层创建Level
        // 即使当前只用到第0层，但预先创建可以避免后续动态扩展的复杂性
        for (int i = 0; i < MAX_LEVEL; i++) {
            this.header.getLevelList().add(new Level<>());
        }

        // tail - 尾节点指针，指向跳跃表中最后一个节点
        // 初始化为null，因为当前跳跃表为空
        this.tail = null;
    }

    /**
     * 生成随机层数
     * 总结：使用概率算法决定新节点应该存在于多少层中
     * 通过随机数和固定概率P来保证跳跃表的平衡性，实现O(log n)的期望性能
     *
     * @return 层数 - 返回值范围[1, MAX_LEVEL]，表示新节点应该存在的层数
     */
    private int randomLevel() {
        // level - 局部变量，表示当前计算得到的层数，从1开始
        int level = 1;

        // 循环条件解析：
        // random.nextDouble() < P：生成[0,1)区间的随机数，如果小于概率P(0.25)则继续升层
        // level < MAX_LEVEL：确保不超过最大层数限制
        // 每次循环都有25%的概率继续，75%的概率停止，这样保证了层数的指数分布
        while (random.nextDouble() < P && level < MAX_LEVEL) {
            // 满足升层条件，层数加1
            level++;
        }

        // 返回最终确定的层数
        return level;
    }

    /**
     * 插入节点
     * 总结：在跳跃表中插入一个新的键值对，同时维护跳跃表的有序性和层级结构
     * 算法分为四个主要步骤：1)查找插入位置 2)确定新节点层数 3)创建并链接新节点 4)更新相关指针和计数器
     *
     * @param score 分数 - 用于排序的数值，跳跃表按score升序排列
     * @param data  数据 - 实际存储的数据对象，当score相同时按data的自然顺序排列
     * @return 插入的节点 - 返回新创建的节点引用，便于调用者获取插入结果
     */
    public SkipListNode<T> insert(double score, T data) {
        // previous数组 - 记录每一层中新节点的前驱节点
        // 在插入时需要修改这些节点的forward指针指向新节点
        SkipListNode<T>[] previous = new SkipListNode[MAX_LEVEL];

        // totalSpan数组 - 记录到达每层previous[i]节点时已经跨越的节点数量
        // 用于计算和更新span（跨度）值，跨度表示forward指针跨越了多少个节点
        int[] totalSpan = new int[MAX_LEVEL];

        // current - 当前遍历的节点，从头节点开始查找插入位置
        SkipListNode<T> current = header;

        // 从最高层开始查找插入位置
        // 采用自顶向下的查找策略，利用高层的"快速通道"减少比较次数
        for (int i = level - 1; i >= 0; i--) {
            // totalSpan[i]的初始化：
            // 如果是最高层(i == level - 1)，从0开始计数
            // 如果不是最高层，继承上一层的rank值，因为上层已经跨越了一些节点
            totalSpan[i] = (i == level - 1) ? 0 : totalSpan[i + 1];

            // 在当前层向前查找，直到找到插入位置
            // 查找条件：下一个节点存在 且 (分数更小 或 分数相等但数据更小)
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                // 累加当前节点在第i层的跨度到rank[i]
                // span表示forward指针跨越的节点数量
                totalSpan[i] += current.getLevelList().get(i).getSpan();

                // 移动到下一个节点继续查找
                current = current.getLevelList().get(i).getForward();
            }

            // 记录第i层的前驱节点，新节点将插入在这个节点之后
            previous[i] = current;
        }

        // 生成新节点的层数
        // 使用随机算法确定新节点应该存在于多少层中
        int newLevel = randomLevel();

        // 如果新节点的层数超过了当前跳跃表的最大层数
        // 需要为新增的层进行初始化
        if (newLevel > level) {
            // 为新增的每一层进行初始化
            for (int i = level; i < newLevel; i++) {
                // 新层的span初始化为0，因为只有头节点到达过这些层
                totalSpan[i] = 0;

                // 新层的前驱节点是头节点
                previous[i] = header;

                // 设置头节点在新层的跨度为当前跳跃表的长度
                // 因为头节点的forward在新层是null，span表示跨越所有现有节点
                // previous[i].getLevelList().get(i).setSpan((int) length);
            }

            // 更新跳跃表的最大层数
            level = newLevel;
        }

        // 创建新节点
        // 传入数据、分数和层数，构造函数会创建对应层数的Level对象
        SkipListNode<T> newNode = new SkipListNode<>(data, score, newLevel);

        // 更新指针和跨度
        // 对新节点存在的每一层进行链接操作
        for (int i = 0; i < newLevel; i++) {
            // 设置新节点第i层的forward指针
            // 指向原来update[i]节点第i层的forward节点
            newNode.getLevelList().get(i)
                .setForward(previous[i].getLevelList().get(i).getForward());

            // 更新前驱节点第i层的forward指针指向新节点
            previous[i].getLevelList().get(i).setForward(newNode);

            // 更新跨度信息
            // 新节点第i层的跨度 = 原前驱节点的跨度 - (第0层到第i层的rank差值)
            // 这个计算确保了跨度的正确性，反映了forward指针实际跨越的节点数
            newNode.getLevelList().get(i).setSpan(
                previous[i].getLevelList().get(i).getSpan() - (totalSpan[0] - totalSpan[i]));

            // 前驱节点第i层的新跨度 = rank差值 + 1
            // +1是因为要跨越新插入的节点
            previous[i].getLevelList().get(i).setSpan(totalSpan[0] - totalSpan[i] + 1);
        }

        // 更新没有新节点的层的跨度
        // 对于新节点不存在的高层，需要增加这些层中相关节点的跨度
        for (int i = newLevel; i < level; i++) {
            // 这些层的update[i]节点的跨度需要+1，因为增加了一个新节点
            previous[i].getLevelList().get(i)
                .setSpan(previous[i].getLevelList().get(i).getSpan() + 1);
        }

        // 更新backward指针（反向指针）
        // 如果新节点的前驱是头节点，则新节点的previous为null
        // 否则指向真正的前驱节点，用于支持反向遍历
        newNode.setPrevious((previous[0] == header) ? null : previous[0]);

        // 更新新节点后继节点的反向指针
        if (newNode.getLevelList().get(0).getForward() != null) {
            // 如果新节点有后继节点，让后继节点的previous指向新节点
            newNode.getLevelList().get(0).getForward().setPrevious(newNode);
        } else {
            // 如果新节点没有后继节点，说明它是最后一个节点，更新tail指针
            tail = newNode;
        }

        // 增加跳跃表的节点计数
        length++;

        // 返回新创建的节点
        return newNode;
    }

    /**
     * 删除节点
     * <p>
     * 总结：从跳跃表中删除指定分数和数据的节点
     * 算法分为两个步骤：1)查找目标节点及其前驱节点 2)调用deleteNode进行实际删除操作
     *
     * @param score 分数 - 要删除节点的分数值
     * @param data  数据 - 要删除节点的数据值，与score一起唯一确定一个节点
     * @return 是否删除成功 - true表示找到并删除了节点，false表示节点不存在
     */
    public boolean delete(double score, T data) {
        // update数组 - 记录每一层中目标节点的前驱节点
        // 删除时需要修改这些节点的指针以跳过被删除的节点
        SkipListNode<T>[] update = new SkipListNode[MAX_LEVEL];

        // current - 当前遍历的节点，从头节点开始查找目标节点
        SkipListNode<T> current = header;

        // 查找要删除的节点
        // 采用与插入相同的查找策略，从高层到低层逐层查找
        for (int i = level - 1; i >= 0; i--) {
            // 在当前层向前移动，直到找到目标位置
            // 查找条件：下一个节点存在 且 (分数更小 或 分数相等但数据更小)
            // 这里的条件与插入时相同，确保找到正确的前驱位置
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                // 移动到下一个节点继续查找
                current = current.getLevelList().get(i).getForward();
            }

            // 记录第i层的前驱节点，如果找到目标节点，这些将是需要更新的节点
            update[i] = current;
        }

        // 移动到可能的目标节点
        // 经过上面的查找，current指向的是目标节点的前驱
        // 所以current.forward[0]指向的可能就是目标节点
        current = current.getLevelList().get(0).getForward();

        // 检查是否找到目标节点
        // 需要同时匹配分数和数据才能确认是目标节点
        if (current != null && current.getScore() == score && current.getData().equals(data)) {
            // 找到目标节点，调用删除方法
            deleteNode(current, update);
            return true;
        }

        // 没有找到目标节点
        return false;
    }

    /**
     * 删除指定节点
     * <p>
     * 总结：执行实际的节点删除操作，更新所有相关的指针和跨度信息
     * 包括：1)更新forward指针和span 2)更新backward指针 3)调整层级结构 4)更新节点计数
     *
     * @param node   要删除的节点 - 已经确定存在的目标节点
     * @param update 更新数组 - 每一层中目标节点的前驱节点数组
     */
    private void deleteNode(SkipListNode<T> node, SkipListNode<T>[] update) {
        // 更新指针和跨度
        // 对跳跃表的每一层进行处理
        for (int i = 0; i < level; i++) {
            // 检查第i层的前驱节点是否直接指向待删除节点
            if (update[i].getLevelList().get(i).getForward() == node) {
                // 直接指向：需要更新前驱节点的forward指针和span
                // 新的span = 前驱的span + 待删除节点的span - 1
                // -1是因为删除了一个节点
                update[i].getLevelList().get(i).setSpan(
                    update[i].getLevelList().get(i).getSpan() + node.getLevelList().get(i).getSpan()
                        - 1);

                // 让前驱节点的forward指针跳过待删除节点，直接指向待删除节点的下一个节点
                update[i].getLevelList().get(i).setForward(node.getLevelList().get(i).getForward());
            } else {
                // 不直接指向：说明这一层的前驱节点通过更高层跳过了待删除节点
                // 只需要将span减1，因为总节点数减少了1
                update[i].getLevelList().get(i)
                    .setSpan(update[i].getLevelList().get(i).getSpan() - 1);
            }
        }

        // 更新backward指针（反向指针）
        // 检查待删除节点是否有后继节点
        if (node.getLevelList().get(0).getForward() != null) {
            // 有后继节点：让后继节点的previous指针指向待删除节点的前驱
            node.getLevelList().get(0).getForward().setPrevious(node.getPrevious());
        } else {
            // 没有后继节点：说明待删除节点是尾节点，更新tail指针
            tail = node.getPrevious();
        }

        // 删除空的层
        // 如果删除节点后某些高层变为空层，需要减少跳跃表的层数
        // 条件：层数大于1 且 头节点在最高层没有forward指针
        while (level > 1 && header.getLevelList().get(level - 1).getForward() == null) {
            // 减少一层
            level--;
        }

        // 减少跳跃表的节点计数
        length--;
    }

    /**
     * 查找节点
     * <p>
     * 总结：在跳跃表中查找指定分数和数据的节点
     * 使用与插入、删除相同的多层查找策略，从高层开始快速定位到目标位置
     *
     * @param score 分数 - 目标节点的分数值
     * @param data  数据 - 目标节点的数据值，与score一起唯一标识节点
     * @return 找到的节点，如果不存在返回null
     */
    public SkipListNode<T> search(double score, T data) {
        // current - 当前遍历的节点，从头节点开始查找
        SkipListNode<T> current = header;

        // 从最高层开始查找目标节点
        // 利用跳跃表的分层结构实现快速查找
        for (int i = level - 1; i >= 0; i--) {
            // 在当前层向前移动，寻找目标位置
            // 移动条件：下一个节点存在 且 (分数更小 或 分数相等但数据更小)
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        < 0))) {
                // 移动到下一个节点
                current = current.getLevelList().get(i).getForward();
            }
        }

        // 移动到可能的目标节点
        // 经过多层查找后，current指向目标节点的前驱
        // current.forward[0]可能就是目标节点
        current = current.getLevelList().get(0).getForward();

        // 验证是否找到目标节点
        // 需要精确匹配分数和数据
        if (current != null && current.getScore() == score && current.getData().equals(data)) {
            return current;
        }

        // 没有找到目标节点
        return null;
    }

    /**
     * 根据排名获取节点（从1开始）
     * <p>
     * 总结：根据在跳跃表中的排名位置获取对应的节点
     * 利用每层的span信息快速跳跃，避免逐个遍历，实现O(log n)的查找效率
     *
     * @param rank 排名 - 目标节点的位置，从1开始计数（1表示第一个节点）
     * @return 节点 - 排名对应的节点，如果排名无效返回null
     */
    public SkipListNode<T> getByRank(long rank) {
        // 参数验证：排名必须在有效范围内
        if (rank < 1 || rank > length) {
            return null;
        }

        // current - 当前遍历的节点，从头节点开始
        SkipListNode<T> current = header;

        // traversed - 已经遍历过的节点数量，用于跟踪当前位置
        long traversed = 0;

        // 从最高层开始查找目标排名
        // 高层的大跨度可以快速接近目标位置
        for (int i = level - 1; i >= 0; i--) {
            // 在当前层尽可能向前跳跃
            // 条件：下一个节点存在 且 跳跃后不会超过目标排名
            while (current.getLevelList().get(i).getForward() != null
                && (traversed + current.getLevelList().get(i).getSpan()) <= rank) {

                // 累加跨度到已遍历数量
                // span表示forward指针跨越的节点数量
                traversed += current.getLevelList().get(i).getSpan();

                // 跳跃到下一个节点
                current = current.getLevelList().get(i).getForward();
            }

            // 检查是否正好到达目标排名
            if (traversed == rank) {
                return current;
            }
        }

        // 理论上不应该到达这里，因为前面已经做了范围检查
        return null;
    }

    /**
     * 获取节点的排名
     * <p>
     * 总结：计算指定节点在跳跃表中的排名位置
     * 通过遍历查找目标节点，同时累计span值来计算排名
     *
     * @param score 分数 - 目标节点的分数值
     * @param data  数据 - 目标节点的数据值
     * @return 排名（从1开始），如果不存在返回0
     */
    public long getRank(double score, T data) {
        // current - 当前遍历的节点，从头节点开始
        SkipListNode<T> current = header;

        // rank - 累计的排名值，记录已经跨越的节点数量
        long rank = 0;

        // 从最高层开始查找目标节点
        for (int i = level - 1; i >= 0; i--) {
            // 在当前层向前移动，累计排名
            // 移动条件：下一个节点存在 且 (分数更小 或 分数相等但数据不大于目标)
            // 注意这里用的是<=，这样可以正确处理重复分数的情况
            while (current.getLevelList().get(i).getForward() != null && (
                current.getLevelList().get(i).getForward().getScore() < score || (
                    current.getLevelList().get(i).getForward().getScore() == score
                        && current.getLevelList().get(i).getForward().getData().compareTo(data)
                        <= 0))) {

                // 累加当前节点第i层的跨度
                rank += current.getLevelList().get(i).getSpan();

                // 移动到下一个节点
                current = current.getLevelList().get(i).getForward();
            }

            // 检查当前节点是否就是目标节点
            // 需要验证数据和分数都匹配
            if (current.getData() != null && current.getData().equals(data)
                && current.getScore() == score) {
                return rank;
            }
        }

        // 没有找到目标节点，返回0表示不存在
        return 0;
    }

    /**
     * 判断跳跃表是否为空
     * <p>
     * 总结：检查跳跃表中是否包含任何数据节点
     * 通过检查length字段来快速判断，避免遍历整个结构
     *
     * @return 是否为空 - true表示空表，false表示包含至少一个节点
     */
    public boolean isEmpty() {
        // length记录了跳跃表中数据节点的数量
        // 0表示空表，大于0表示非空
        return length == 0;
    }

    /**
     * 清空跳跃表
     * <p>
     * 总结：重置跳跃表到初始状态，删除所有数据节点
     * 重新初始化头节点和相关字段，等效于创建一个新的空跳跃表
     */
    public void clear() {
        // 重新创建头节点，原有的所有数据节点将被垃圾回收
        header = new SkipListNode<>();

        // 为新头节点的每一层创建Level对象
        // 这样做是为了保持结构的一致性，避免空指针异常
        for (int i = 0; i < MAX_LEVEL; i++) {
            this.header.getLevelList().add(new Level<>());
        }

        // 重置尾节点指针，因为现在没有任何数据节点
        tail = null;

        // 重置层数为1（只有第0层）
        level = 1;

        // 重置节点计数为0
        length = 0;
    }

    /**
     * 打印跳跃表结构（用于调试）
     * <p>
     * 总结：输出跳跃表的层级结构，便于调试和理解数据分布
     * 从最高层到最低层逐层打印，显示每层包含的节点信息
     */
    public void print() {
        // 打印跳跃表的基本信息：节点数量和层数
        System.out.println("SkipList [length=" + length + ", level=" + level + "]");

        // 从最高层开始打印，这样便于观察层级结构
        for (int i = level - 1; i >= 0; i--) {
            // 打印当前层的标识
            System.out.print("Level " + i + ": ");

            // current指向当前层的第一个数据节点
            // 跳过头节点，因为头节点不包含实际数据
            SkipListNode<T> current = header.getLevelList().get(i).getForward();

            // 遍历当前层的所有节点
            while (current != null) {
                // 打印节点的分数和数据，格式：[分数,数据]
                System.out.print("[" + current.getScore() + "," + current.getData() + "] ");

                // 移动到当前层的下一个节点
                current = current.getLevelList().get(i).getForward();
            }

            // 当前层打印完毕，换行
            System.out.println();
        }

        // 打印完所有层后，再输出一个空行作为分隔
        System.out.println();
    }
} 