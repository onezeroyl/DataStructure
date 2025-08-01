# 跳跃表（Skip List）实现

## 概述

跳跃表是一种随机化的数据结构，可以在对数时间内完成查找、插入、删除操作。它通过维护多个层级的链表来实现快速访问，是平衡树的一个替代方案。

## 特点

- **时间复杂度**: 平均情况下，查找、插入、删除操作的时间复杂度都是 O(log n)
- **空间复杂度**: O(n)
- **随机化**: 使用随机数来决定节点的层数，避免了复杂的平衡操作
- **简单实现**: 相比红黑树、AVL树等平衡树，实现更加简单

## 类结构

### SkipListNode<T>
跳跃表的节点类，包含：
- `data`: 存储的数据
- `score`: 分数（用于排序）
- `levelList`: 层级列表
- `previous`: 前向指针

### Level
层级信息类，包含：
- `span`: 跨度（到下一个节点的距离）
- `forward`: 指向下一个节点的指针

### SkipList<T>
跳跃表主类，提供以下主要方法：

#### 插入操作
```java
public SkipListNode<T> insert(double score, T data)
```
- 插入一个新节点
- 时间复杂度: O(log n)

#### 删除操作
```java
public boolean delete(double score, T data)
```
- 删除指定的节点
- 时间复杂度: O(log n)

#### 查找操作
```java
public SkipListNode<T> search(double score, T data)
```
- 查找指定的节点
- 时间复杂度: O(log n)

#### 排名操作
```java
public SkipListNode<T> getByRank(long rank)
public long getRank(double score, T data)
```
- 根据排名获取节点（从1开始）
- 获取节点的排名
- 时间复杂度: O(log n)

## 使用示例

```java
// 创建跳跃表
SkipList<String> skipList = new SkipList<>();

// 插入数据
skipList.insert(90, "Alice");
skipList.insert(85, "Bob");
skipList.insert(95, "Charlie");

// 查找数据
SkipListNode<String> found = skipList.search(90, "Alice");
if (found != null) {
    System.out.println("找到: " + found.getData());
}

// 获取排名
long rank = skipList.getRank(90, "Alice");
System.out.println("Alice的排名: " + rank);

// 根据排名获取节点
SkipListNode<String> first = skipList.getByRank(1);
System.out.println("第1名: " + first.getData());

// 删除数据
boolean deleted = skipList.delete(85, "Bob");
System.out.println("删除成功: " + deleted);
```

## 运行演示

可以运行 `SkipListDemo` 类来查看完整的演示：

```bash
java com.edws.datastructure.skiplist.SkipListDemo
```

## 运行测试

项目包含完整的单元测试，可以验证跳跃表的正确性：

```bash
mvn test -Dtest=SkipListTest
```

## 算法原理

### 层数生成
- 使用随机数生成节点层数
- 每层的概率为 0.25
- 最大层数限制为 32

### 查找算法
1. 从最高层开始
2. 在每一层中向前移动，直到下一个节点的值大于目标值
3. 下降到下一层继续查找
4. 重复直到找到目标或到达底层

### 插入算法
1. 使用查找算法确定插入位置
2. 生成新节点的随机层数
3. 更新各层的指针和跨度信息
4. 更新前向和后向指针

### 删除算法
1. 使用查找算法找到目标节点
2. 更新各层的指针和跨度信息
3. 删除空的层
4. 更新长度

## 性能特点

- **查找**: O(log n) 平均时间复杂度
- **插入**: O(log n) 平均时间复杂度  
- **删除**: O(log n) 平均时间复杂度
- **空间**: O(n) 空间复杂度
- **支持排名操作**: 可以高效地进行范围查询和排名操作

## 应用场景

跳跃表特别适用于以下场景：
- 需要保持数据有序的场景
- 需要支持范围查询的场景
- 需要支持排名操作的场景
- 实现有序集合数据结构
- Redis 的有序集合（ZSET）底层实现就使用了跳跃表 