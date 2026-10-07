package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FineSummaryDTO {
    private long totalFines;
    private long unpaidFines;
    private long unpaidAmount;
    private long paidFines;
    private long paidAmount;
}
