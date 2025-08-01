package com.edws.datastructure.skiplist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 跳跃表单元测试
 * @author whs
 */
public class SkipListTest {

    private SkipList<String> skipList;

    @BeforeEach
    public void setUp() {
        skipList = new SkipList<>();
    }

    @Test
    public void testInsertAndSearch() {
        // 测试插入和查找
        skipList.insert(90, "Alice");
        skipList.insert(85, "Bob");
        skipList.insert(95, "Charlie");

        assertEquals(3, skipList.getLength());
        assertFalse(skipList.isEmpty());

        // 测试查找存在的元素
        SkipListNode<String> found = skipList.search(90, "Alice");
        assertNotNull(found);
        assertEquals("Alice", found.getData());
        assertEquals(90.0, found.getScore());

        // 测试查找不存在的元素
        found = skipList.search(80, "David");
        assertNull(found);
    }

    @Test
    public void testDelete() {
        // 插入数据
        skipList.insert(90, "Alice");
        skipList.insert(85, "Bob");
        skipList.insert(95, "Charlie");

        assertEquals(3, skipList.getLength());

        // 删除存在的元素
        boolean deleted = skipList.delete(85, "Bob");
        assertTrue(deleted);
        assertEquals(2, skipList.getLength());

        // 验证删除后无法找到
        SkipListNode<String> found = skipList.search(85, "Bob");
        assertNull(found);

        // 删除不存在的元素
        deleted = skipList.delete(80, "David");
        assertFalse(deleted);
        assertEquals(2, skipList.getLength());
    }

    @Test
    public void testRanking() {
        // 插入有序数据
        skipList.insert(85, "Bob");
        skipList.insert(90, "Alice");
        skipList.insert(95, "Charlie");

        // 测试获取排名
        assertEquals(1, skipList.getRank(85, "Bob"));
        assertEquals(2, skipList.getRank(90, "Alice"));
        assertEquals(3, skipList.getRank(95, "Charlie"));
        assertEquals(0, skipList.getRank(80, "David")); // 不存在的元素

        // 测试根据排名获取节点
        SkipListNode<String> first = skipList.getByRank(1);
        assertNotNull(first);
        assertEquals("Bob", first.getData());
        assertEquals(85.0, first.getScore());

        SkipListNode<String> third = skipList.getByRank(3);
        assertNotNull(third);
        assertEquals("Charlie", third.getData());
        assertEquals(95.0, third.getScore());

        // 测试无效排名
        assertNull(skipList.getByRank(0));
        assertNull(skipList.getByRank(4));
    }

    @Test
    public void testSameScore() {
        // 测试相同分数的元素
        skipList.insert(90, "Alice");
        skipList.insert(90, "Bob");
        skipList.insert(90, "Charlie");

        assertEquals(3, skipList.getLength());

        // 相同分数按字典序排列
        SkipListNode<String> first = skipList.getByRank(1);
        assertEquals("Alice", first.getData());

        SkipListNode<String> second = skipList.getByRank(2);
        assertEquals("Bob", second.getData());

        SkipListNode<String> third = skipList.getByRank(3);
        assertEquals("Charlie", third.getData());
    }

    @Test
    public void testClear() {
        // 插入数据
        skipList.insert(90, "Alice");
        skipList.insert(85, "Bob");
        skipList.insert(95, "Charlie");

        assertEquals(3, skipList.getLength());
        assertFalse(skipList.isEmpty());

        // 清空
        skipList.clear();

        assertEquals(0, skipList.getLength());
        assertTrue(skipList.isEmpty());
        assertEquals(1, skipList.getLevel());

        // 验证清空后无法找到任何元素
        assertNull(skipList.search(90, "Alice"));
        assertNull(skipList.getByRank(1));
    }

    @Test
    public void testLargeDataSet() {
        // 测试大数据集
        int size = 1000;
        for (int i = 0; i < size; i++) {
            skipList.insert(i, "Item" + i);
        }

        assertEquals(size, skipList.getLength());

        // 随机测试一些元素
        for (int i = 0; i < 100; i++) {
            int index = (int) (Math.random() * size);
            SkipListNode<String> found = skipList.search(index, "Item" + index);
            assertNotNull(found);
            assertEquals("Item" + index, found.getData());
            assertEquals((double) index, found.getScore());
        }

        // 测试排名
        for (int i = 1; i <= Math.min(10, size); i++) {
            SkipListNode<String> node = skipList.getByRank(i);
            assertNotNull(node);
            assertEquals(i - 1, (int) node.getScore());
        }
    }

    @Test
    public void testIntegerSkipList() {
        // 测试整数类型的跳跃表
        SkipList<Integer> intSkipList = new SkipList<>();

        intSkipList.insert(3.14, 100);
        intSkipList.insert(2.71, 200);
        intSkipList.insert(1.41, 300);

        assertEquals(3, intSkipList.getLength());

        SkipListNode<Integer> found = intSkipList.search(2.71, 200);
        assertNotNull(found);
        assertEquals(Integer.valueOf(200), found.getData());
        assertEquals(2.71, found.getScore());
    }
} 