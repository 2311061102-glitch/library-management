package vn.edu.crs.librarymanagement.service;

import org.springframework.stereotype.Service;
import vn.edu.crs.librarymanagement.entity.Fine;
import vn.edu.crs.librarymanagement.repository.FineRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FineService {

    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    public List<Fine> getFinesByUser(Long userId) {
        return fineRepository.findByBorrowRecord_User_Id(userId);
    }

    public List<Fine> getAllFines() {
        return fineRepository.findAll();
    }

    public Optional<Fine> getFineById(Long id) {
        return fineRepository.findById(id);
    }

    // ===== Đánh dấu đã thanh toán (chỉ ADMIN) =====
    public Fine markAsPaid(String role, Long id) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Ban khong co quyen thuc hien thao tac nay");
        }
        Fine fine = fineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay phieu phat"));
        fine.setStatus("PAID");
        return fineRepository.save(fine);
    }
}