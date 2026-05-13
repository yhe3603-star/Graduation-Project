package com.dongmedicine.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("敏感词过滤器测试")
class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        filter.init();
    }

    @Test
    @DisplayName("matchLevel - 高危敏感词返回HIGH")
    void testMatchLevel_High() {
        assertEquals(SensitiveWordFilter.SensitiveLevel.HIGH, filter.matchLevel("你这个傻逼"));
        assertEquals(SensitiveWordFilter.SensitiveLevel.HIGH, filter.matchLevel("出售手枪"));
        assertEquals(SensitiveWordFilter.SensitiveLevel.HIGH, filter.matchLevel("炸药配方"));
    }

    @Test
    @DisplayName("matchLevel - 普通敏感词返回NORMAL")
    void testMatchLevel_Normal() {
        assertEquals(SensitiveWordFilter.SensitiveLevel.NORMAL, filter.matchLevel("兼职赚钱"));
        assertEquals(SensitiveWordFilter.SensitiveLevel.NORMAL, filter.matchLevel("招聘客服"));
    }

    @Test
    @DisplayName("matchLevel - 无敏感词返回NONE")
    void testMatchLevel_None() {
        assertEquals(SensitiveWordFilter.SensitiveLevel.NONE, filter.matchLevel("侗药文化很有趣"));
        assertEquals(SensitiveWordFilter.SensitiveLevel.NONE, filter.matchLevel("这是正常评论"));
    }

    @Test
    @DisplayName("matchLevel - null和空返回NONE")
    void testMatchLevel_NullAndEmpty() {
        assertEquals(SensitiveWordFilter.SensitiveLevel.NONE, filter.matchLevel(null));
        assertEquals(SensitiveWordFilter.SensitiveLevel.NONE, filter.matchLevel(""));
        assertEquals(SensitiveWordFilter.SensitiveLevel.NONE, filter.matchLevel("   "));
    }

    @Test
    @DisplayName("containsSensitiveWord - 两个词库都能命中")
    void testContainsSensitiveWord_True() {
        assertTrue(filter.containsSensitiveWord("你这个傻逼"));
        assertTrue(filter.containsSensitiveWord("垃圾东西"));
        assertTrue(filter.containsSensitiveWord("去死吧"));
        assertTrue(filter.containsSensitiveWord("兼职赚钱"));
    }

    @Test
    @DisplayName("containsSensitiveWord - 不包含敏感词返回false")
    void testContainsSensitiveWord_False() {
        assertFalse(filter.containsSensitiveWord("侗药文化很有趣"));
        assertFalse(filter.containsSensitiveWord("这是正常评论"));
    }

    @Test
    @DisplayName("containsSensitiveWord - null和空字符串返回false")
    void testContainsSensitiveWord_NullAndEmpty() {
        assertFalse(filter.containsSensitiveWord(null));
        assertFalse(filter.containsSensitiveWord(""));
        assertFalse(filter.containsSensitiveWord("   "));
    }

    @Test
    @DisplayName("匹配敏感词 - 返回所有匹配的词")
    void testMatch_ReturnsAllMatches() {
        List<String> matched = filter.match("你这个傻逼真是个垃圾");
        assertFalse(matched.isEmpty());
    }

    @Test
    @DisplayName("匹配敏感词 - 无匹配返回空列表")
    void testMatch_NoMatch() {
        List<String> matched = filter.match("正常评论内容");
        assertTrue(matched.isEmpty());
    }

    @Test
    @DisplayName("匹配敏感词 - null和空返回空列表")
    void testMatch_NullAndEmpty() {
        assertTrue(filter.match(null).isEmpty());
        assertTrue(filter.match("").isEmpty());
    }

    @Test
    @DisplayName("大小写不敏感匹配")
    void testMatch_CaseInsensitive() {
        assertTrue(filter.containsSensitiveWord("傻B"));
        assertTrue(filter.containsSensitiveWord("傻b"));
    }
}
