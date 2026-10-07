export type BorrowStatus = 'BORROWING' | 'RETURNED';

export interface BorrowRecord {
    id: number;
    readerId: number;
    bookId: number;
    bookTitle: string;
    borrowDate: string;
    dueDate: string;
    returnDate: string | null;
    status: BorrowStatus;
    overdue: boolean;
    overdueDays: number;
    estimatedFine: number;
    renewalCount: number;
    remainingRenewals: number;
}

export interface BorrowSummary {
    totalBorrowed: number;
    activeBorrows: number;
    returnedBorrows: number;
    overdueBorrows: number;
    unpaidFineCount: number;
    unpaidFineAmount: number;
}

export interface Fine {
    id: number;
    borrowRecordId: number;
    readerId: number;
    bookTitle: string;
    overdueDays: number;
    amount: number;
    reason: string;
    paid: boolean;
    createdAt: string;
    paidAt: string | null;
}
