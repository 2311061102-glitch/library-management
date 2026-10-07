package vn.edu.library.borrowservice.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FineCalculatorTest {

    private static final LocalDate DUE = LocalDate.of(2026, 10, 14);

    @Test
    void traDungHan_khongBiPhat() {
        assertEquals(0, FineCalculator.overdueDays(DUE, DUE));
    }

    @Test
    void traSomHon_hanKhongBiPhat() {
        assertEquals(0, FineCalculator.overdueDays(DUE, DUE.minusDays(3)));
    }

    @Test
    void traTre3Ngay_tinhDung3Ngay() {
        assertEquals(3, FineCalculator.overdueDays(DUE, DUE.plusDays(3)));
    }

    @Test
    void tienPhat_bangSoNgayNhanMucPhat() {
        assertEquals(15000L, FineCalculator.fineAmount(3, 5000));
        assertEquals(0L, FineCalculator.fineAmount(0, 5000));
    }
}
