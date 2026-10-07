package vn.edu.library.borrowservice.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Quy tắc tính phạt: mỗi ngày trả trễ bị phạt một mức cố định. Tách riêng để dễ kiểm thử. */
public final class FineCalculator {

    private FineCalculator() {}

    /** Số ngày trễ hạn tại thời điểm {@code asOf}; 0 nếu chưa quá hạn. */
    public static int overdueDays(LocalDate dueDate, LocalDate asOf) {
        long days = ChronoUnit.DAYS.between(dueDate, asOf);
        return days > 0 ? (int) days : 0;
    }

    public static long fineAmount(int overdueDays, long finePerDay) {
        return (long) overdueDays * finePerDay;
    }
}
