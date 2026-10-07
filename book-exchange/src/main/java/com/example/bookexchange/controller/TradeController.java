package com.example.bookexchange.controller;

import com.example.bookexchange.entity.TradeRequest;
import com.example.bookexchange.service.TradeRequestService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*")
public class TradeController {

    private final TradeRequestService tradeRequestService;

    public TradeController(TradeRequestService tradeRequestService) {
        this.tradeRequestService = tradeRequestService;
    }

    @GetMapping
    public List<TradeRequest> getRequests(@RequestParam(required = false) String q) {
        return tradeRequestService.searchRequests(q);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TradeRequest createRequest(@RequestBody TradeRequest tradeRequest) {
        return tradeRequestService.saveRequest(tradeRequest);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TradeRequest createRequestFromForm(@ModelAttribute TradeRequest tradeRequest) {
        return tradeRequestService.saveRequest(tradeRequest);
    }
}
