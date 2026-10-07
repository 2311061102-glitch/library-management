package vn.edu.library.borrowservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.library.borrowservice.client.BookClient;
import vn.edu.library.borrowservice.dto.BorrowRecordDTO;
import vn.edu.library.borrowservice.dto.BorrowRequestDTO;
import vn.edu.library.borrowservice.entity.BorrowRecord;
import vn.edu.library.borrowservice.entity.Fine;
import vn.edu.library.borrowservice.repository.BorrowRecordRepository;
import vn.edu.library.borrowservice.repository.FineRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;
    @Mock
    private FineRepository fineRepository;
    @Mock
    private BookClient bookClient;
    @InjectMocks
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(borrowService, "loanDays", 14);
        ReflectionTestUtils.setField(borrowService, "maxActiveLoans", 5);
        ReflectionTestUtils.setField(borrowService, "finePerDay", 5000L);
    }

    private BorrowRequestDTO request(Long bookId) {
        BorrowRequestDTO dto = new BorrowRequestDTO();
        dto.setBookId(bookId);
        return dto;
    }

    @Test
    void borrow_thanhCong_luuPhieuVaHanTra14Ngay() {
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(false);
        when(borrowRecordRepository.countByReaderIdAndStatus(1L, BorrowRecord.BORROWING)).thenReturn(0L);
        when(borrowRecordRepository.existsByReaderIdAndBookIdAndStatus(1L, 10L, BorrowRecord.BORROWING)).thenReturn(false);
        when(bookClient.reserveCopy(10L)).thenReturn("Clean Code");
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRecordDTO result = borrowService.borrow(request(10L), 1L, false);

        assertEquals("Clean Code", result.getBookTitle());
        assertEquals(BorrowRecord.BORROWING, result.getStatus());
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
    }

    @Test
    void borrow_docGiaKhongTheMuonHoNguoiKhac_luonDungUserIdTuJwt() {
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(false);
        when(borrowRecordRepository.countByReaderIdAndStatus(1L, BorrowRecord.BORROWING)).thenReturn(0L);
        when(borrowRecordRepository.existsByReaderIdAndBookIdAndStatus(1L, 10L, BorrowRecord.BORROWING)).thenReturn(false);
        when(bookClient.reserveCopy(10L)).thenReturn("Clean Code");
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRequestDTO dto = request(10L);
        dto.setReaderId(999L); // cố tình giả mạo readerId

        BorrowRecordDTO result = borrowService.borrow(dto, 1L, false);

        assertEquals(1L, result.getReaderId());
    }

    @Test
    void borrow_conNoPhat_biTuChoiVaKhongTruBan() {
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> borrowService.borrow(request(10L), 1L, false));
        verify(bookClient, never()).reserveCopy(any());
    }

    @Test
    void borrow_datGioiHanDangMuon_biTuChoi() {
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(false);
        when(borrowRecordRepository.countByReaderIdAndStatus(1L, BorrowRecord.BORROWING)).thenReturn(5L);

        assertThrows(IllegalStateException.class, () -> borrowService.borrow(request(10L), 1L, false));
        verify(bookClient, never()).reserveCopy(any());
    }

    @Test
    void borrow_luuPhieuLoi_hoanTraBanSachDaTru() {
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(false);
        when(borrowRecordRepository.countByReaderIdAndStatus(1L, BorrowRecord.BORROWING)).thenReturn(0L);
        when(borrowRecordRepository.existsByReaderIdAndBookIdAndStatus(1L, 10L, BorrowRecord.BORROWING)).thenReturn(false);
        when(bookClient.reserveCopy(10L)).thenReturn("Clean Code");
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> borrowService.borrow(request(10L), 1L, false));
        verify(bookClient).releaseCopy(10L);
    }

    @Test
    void returnBook_triTre_taoKhoanPhat() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now().minusDays(20), LocalDate.now().minusDays(6), null, BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        borrowService.returnBook(5L, 1L, false);

        ArgumentCaptor<Fine> captor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor.capture());
        assertEquals(6, captor.getValue().getOverdueDays());
        assertEquals(30000L, captor.getValue().getAmount());
        assertFalse(captor.getValue().getPaid());
        verify(bookClient).releaseCopy(10L);
    }

    @Test
    void returnBook_dungHan_khongTaoKhoanPhat() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now().minusDays(3), LocalDate.now().plusDays(11), null, BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowRecordDTO result = borrowService.returnBook(5L, 1L, false);

        assertEquals(BorrowRecord.RETURNED, result.getStatus());
        verify(fineRepository, never()).save(any());
    }

    @Test
    void returnBook_docGiaKhacKhongDuocTra() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now(), LocalDate.now().plusDays(14), null, BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(AccessDeniedException.class, () -> borrowService.returnBook(5L, 2L, false));
        verify(bookClient, never()).releaseCopy(any());
    }

    @Test
    void returnBook_thuThuDuocTraHo() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now(), LocalDate.now().plusDays(14), null, BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(BorrowRecord.RETURNED, borrowService.returnBook(5L, 99L, true).getStatus());
    }

    @Test
    void returnBook_daTraRoi_biTuChoi() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now().minusDays(5), LocalDate.now().plusDays(9), LocalDateTime.now(), BorrowRecord.RETURNED);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(IllegalStateException.class, () -> borrowService.returnBook(5L, 1L, false));
    }

    @Test
    void renew_conHan_tangHanTraVaSoLanGiaHan() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now().minusDays(3), LocalDate.now().plusDays(11), null,
                BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));
        when(fineRepository.existsByReaderIdAndPaidFalse(1L)).thenReturn(false);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        ReflectionTestUtils.setField(borrowService, "maxRenewals", 1);

        BorrowRecordDTO result = borrowService.renew(5L, 1L, false);

        assertEquals(LocalDate.now().plusDays(25), result.getDueDate());
        assertEquals(1, result.getRenewalCount());
        assertEquals(0, result.getRemainingRenewals());
    }

    @Test
    void renew_daHetSoLan_biTuChoi() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now(), LocalDate.now().plusDays(10), null,
                BorrowRecord.BORROWING);
        record.setRenewalCount(1);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));
        ReflectionTestUtils.setField(borrowService, "maxRenewals", 1);

        assertThrows(IllegalStateException.class, () -> borrowService.renew(5L, 1L, false));
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void getById_nguoiKhacKhongDuocXem() {
        BorrowRecord record = new BorrowRecord(5L, 1L, 10L, "Clean Code",
                LocalDateTime.now(), LocalDate.now().plusDays(14), null,
                BorrowRecord.BORROWING);
        when(borrowRecordRepository.findById(5L)).thenReturn(Optional.of(record));

        assertThrows(AccessDeniedException.class,
                () -> borrowService.getById(5L, 2L, false));
    }
}
