package com.insurance.backend.service;

import com.insurance.backend.domain.metadata.currency.Currency;
import com.insurance.backend.infrastructure.persistence.repository.metadata.currency.CurrencyRepository;
import com.insurance.backend.web.dto.metadata.currency.CurrencyCreateRequest;
import com.insurance.backend.web.dto.metadata.currency.CurrencyResponse;
import com.insurance.backend.web.dto.metadata.currency.CurrencyUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.MetadataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);

    private final CurrencyRepository currencyRepository;
    private final MetadataMapper metadataMapper;

    public CurrencyService(CurrencyRepository currencyRepository, MetadataMapper metadataMapper) {
        this.currencyRepository = currencyRepository;
        this.metadataMapper = metadataMapper;
    }

    @Transactional
    public CurrencyResponse create(CurrencyCreateRequest request) {
        String code = request.code().trim().toUpperCase();

        if (currencyRepository.existsByCode(code)) {
            throw new ConflictException("Currency code already exists: " + code);
        }

        Currency currency = new Currency(code, request.name().trim(), request.exchangeRateToBase(), request.active());
        Currency saved = currencyRepository.save(currency);

        log.info("Currency created: id={}, code={}", saved.getId(), saved.getCode());
        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CurrencyResponse getById(Long currencyId) {
        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new NotFoundException("Currency not found: " + currencyId));

        return metadataMapper.toResponse(currency);
    }

    @Transactional
    public CurrencyResponse update(Long currencyId, CurrencyUpdateRequest request) {
        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new NotFoundException("Currency not found: " + currencyId));

        metadataMapper.applyUpdate(currency, request);

        Currency saved = currencyRepository.save(currency);
        log.info("Currency updated: id={}, code={}", saved.getId(), saved.getCode());

        return metadataMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CurrencyResponse> list(Pageable pageable) {
        return currencyRepository.findAll(pageable).map(metadataMapper::toResponse);
    }
}
