package com.example.bookexchange.service;

import com.example.bookexchange.entity.TradeRequest;
import com.example.bookexchange.repository.TradeRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TradeRequestService {

    private final TradeRequestRepository tradeRequestRepository;

    public TradeRequestService(TradeRequestRepository tradeRequestRepository) {
        this.tradeRequestRepository = tradeRequestRepository;
    }

    public List<TradeRequest> getAllRequests() {
        return tradeRequestRepository.findAll();
    }

    public TradeRequest saveRequest(TradeRequest tradeRequest) {
        return tradeRequestRepository.save(tradeRequest);
    }

    public List<TradeRequest> searchRequests(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllRequests();
        }
        return tradeRequestRepository
                .findByCustomerNameContainingIgnoreCaseOrPhoneContaining(keyword, keyword);
    }
}
