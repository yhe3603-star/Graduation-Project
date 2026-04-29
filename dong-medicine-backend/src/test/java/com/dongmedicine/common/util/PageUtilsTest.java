package com.dongmedicine.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分页工具类测试")
class PageUtilsTest {

    @Test
    @DisplayName("getPage - 默认值")
    void testGetPage_DefaultValues() {
        Page<Object> page = PageUtils.getPage(null, null);
        assertEquals(1, page.getCurrent());
        assertEquals(20, page.getSize());
    }

    @Test
    @DisplayName("getPage - 正常分页参数")
    void testGetPage_NormalParams() {
        Page<Object> page = PageUtils.getPage(3, 10);
        assertEquals(3, page.getCurrent());
        assertEquals(10, page.getSize());
    }

    @Test
    @DisplayName("getPage - page为0时限制为1")
    void testGetPage_PageZero() {
        Page<Object> page = PageUtils.getPage(0, 10);
        assertEquals(1, page.getCurrent());
    }

    @Test
    @DisplayName("getPage - page为负数时限制为1")
    void testGetPage_PageNegative() {
        Page<Object> page = PageUtils.getPage(-5, 10);
        assertEquals(1, page.getCurrent());
    }

    @Test
    @DisplayName("getPage - size为0时限制为1")
    void testGetPage_SizeZero() {
        Page<Object> page = PageUtils.getPage(1, 0);
        assertEquals(1, page.getSize());
    }

    @Test
    @DisplayName("getPage - size为负数时限制为1")
    void testGetPage_SizeNegative() {
        Page<Object> page = PageUtils.getPage(1, -10);
        assertEquals(1, page.getSize());
    }

    @Test
    @DisplayName("getPage - size超过100时限制为100")
    void testGetPage_SizeOverMax() {
        Page<Object> page = PageUtils.getPage(1, 9999);
        assertEquals(100, page.getSize());
    }

    @Test
    @DisplayName("getPage - size为100时正常")
    void testGetPage_SizeExactlyMax() {
        Page<Object> page = PageUtils.getPage(1, 100);
        assertEquals(100, page.getSize());
    }

    @Test
    @DisplayName("escapeLike - null返回null")
    void testEscapeLike_Null() {
        assertNull(PageUtils.escapeLike(null));
    }

    @Test
    @DisplayName("escapeLike - 空字符串返回空")
    void testEscapeLike_Empty() {
        assertEquals("", PageUtils.escapeLike(""));
    }

    @Test
    @DisplayName("escapeLike - 转义百分号")
    void testEscapeLike_Percent() {
        assertEquals("\\%", PageUtils.escapeLike("%"));
    }

    @Test
    @DisplayName("escapeLike - 转义下划线")
    void testEscapeLike_Underscore() {
        assertEquals("\\_", PageUtils.escapeLike("_"));
    }

    @Test
    @DisplayName("escapeLike - 转义反斜杠")
    void testEscapeLike_Backslash() {
        assertEquals("\\\\", PageUtils.escapeLike("\\"));
    }

    @Test
    @DisplayName("escapeLike - 复合转义")
    void testEscapeLike_Complex() {
        assertEquals("test\\\\\\%\\_value", PageUtils.escapeLike("test\\%_value"));
    }

    @Test
    @DisplayName("toMap - 正常转换")
    void testToMap() {
        Page<String> page = new Page<>(2, 10, 50);
        page.setRecords(java.util.List.of("a", "b"));

        Map<String, Object> map = PageUtils.toMap(page);

        assertEquals(java.util.List.of("a", "b"), map.get("records"));
        assertEquals(50L, map.get("total"));
        assertEquals(2L, map.get("page"));
        assertEquals(10L, map.get("size"));
    }
}
