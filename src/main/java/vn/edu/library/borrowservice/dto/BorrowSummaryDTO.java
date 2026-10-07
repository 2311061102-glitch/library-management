package vn.edu.library.borrowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BorrowSummaryDTO {
    private long totalBorrowed;
    private long activeBorrows;
    private long returnedBorrows;
    private long overdueBorrows;
    private long unpaidFineCount;
    private long unpaidFineAmount;
}
