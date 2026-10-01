package vn.edu.crs.librarymanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_renewals")
public class LoanRenewal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "borrow_record_id", nullable = false)
    private BorrowRecord borrowRecord;

    @Column(name = "old_due_date", nullable = false)
    private LocalDateTime oldDueDate;

    @Column(name = "new_due_date", nullable = false)
    private LocalDateTime newDueDate;

    @Column(name = "renewed_at", nullable = false)
    private LocalDateTime renewedAt;

    public Long getId() { return id; }
    public BorrowRecord getBorrowRecord() { return borrowRecord; }
    public void setBorrowRecord(BorrowRecord borrowRecord) { this.borrowRecord = borrowRecord; }
    public LocalDateTime getOldDueDate() { return oldDueDate; }
    public void setOldDueDate(LocalDateTime oldDueDate) { this.oldDueDate = oldDueDate; }
    public LocalDateTime getNewDueDate() { return newDueDate; }
    public void setNewDueDate(LocalDateTime newDueDate) { this.newDueDate = newDueDate; }
    public LocalDateTime getRenewedAt() { return renewedAt; }
    public void setRenewedAt(LocalDateTime renewedAt) { this.renewedAt = renewedAt; }
}
