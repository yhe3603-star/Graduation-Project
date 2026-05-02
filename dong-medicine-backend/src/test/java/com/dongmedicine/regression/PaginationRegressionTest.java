package com.dongmedicine.regression;

import com.dongmedicine.common.util.PageUtils;
import com.dongmedicine.common.util.XssUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("回归测试 - Pagination")
class PaginationRegressionTest {
@Nested
    @DisplayName("Bug: 列表接口默认查全量数据(size=9999)")
    class PaginationBypassBug {

        @Test
        @DisplayName("size=9999应被限制为100")
        void size9999ShouldBeCapped() {
            var page = PageUtils.getPage(1, 9999);
            assertEquals(100, page.getSize());
        }

        @Test
        @DisplayName("size=1000应被限制为100")
        void size1000ShouldBeCapped() {
            var page = PageUtils.getPage(1, 1000);
            assertEquals(100, page.getSize());
        }

        @Test
        @DisplayName("size=0应被限制为1")
        void size0ShouldBeCapped() {
            var page = PageUtils.getPage(1, 0);
            assertEquals(1, page.getSize());
        }

        @Test
        @DisplayName("page=-1应被限制为1")
        void negativePageShouldBeCapped() {
            var page = PageUtils.getPage(-1, 10);
            assertEquals(1, page.getCurrent());
        }

        @Test
        @DisplayName("page=0应被限制为1")
        void zeroPageShouldBeCapped() {
            var page = PageUtils.getPage(0, 10);
            assertEquals(1, page.getCurrent());
        }

        @Test
        @DisplayName("null参数应使用默认值")
        void nullParamsShouldUseDefaults() {
            var page = PageUtils.getPage(null, null);
            assertEquals(1, page.getCurrent());
            assertEquals(20, page.getSize());
        }

        @ParameterizedTest
        @DisplayName("各种size边界值应被正确限制")
        @ValueSource(ints = {1, 10, 50, 99, 100, 101, 500, 9999, Integer.MAX_VALUE})
        void variousSizeBoundaryValues(int size) {
            var page = PageUtils.getPage(1, size);
            assertTrue(page.getSize() >= 1 && page.getSize() <= 100,
                    "size=" + size + " result=" + page.getSize() + " should be in [1,100]");
        }
    }

}
