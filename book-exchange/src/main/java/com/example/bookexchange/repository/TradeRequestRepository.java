package com.example.bookexchange.repository;

import com.example.bookexchange.entity.TradeRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {

    List<TradeRequest> findByCustomerNameContainingIgnoreCaseOrPhoneContaining(String customerName, String phone);
}
