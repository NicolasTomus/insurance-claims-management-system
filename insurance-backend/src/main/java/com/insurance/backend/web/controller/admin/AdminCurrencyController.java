package com.insurance.backend.web.controller.admin;

import com.insurance.backend.service.CurrencyService;
import com.insurance.backend.web.dto.metadata.currency.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/currencies")
public class AdminCurrencyController {

    private final CurrencyService currencyService;

    public AdminCurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public CurrencyResponse create(@Valid @RequestBody CurrencyCreateRequest request) {
        return currencyService.create(request);
    }

    @GetMapping("/{currencyId}")
    public CurrencyResponse get(@PathVariable Long currencyId) {
        return currencyService.getById(currencyId);
    }

    @PutMapping("/{currencyId}")
    public CurrencyResponse update(@PathVariable Long currencyId, @Valid @RequestBody CurrencyUpdateRequest request) {
        return currencyService.update(currencyId, request);
    }

    @GetMapping
    public Page<CurrencyResponse> list(Pageable pageable) {
        return currencyService.list(pageable);
    }
}
