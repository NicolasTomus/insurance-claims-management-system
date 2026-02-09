package com.insurance.backend.service;

import com.insurance.backend.domain.broker.Broker;
import com.insurance.backend.domain.broker.BrokerStatus;
import com.insurance.backend.infrastructure.persistence.repository.broker.BrokerRepository;
import com.insurance.backend.web.dto.broker.BrokerCreateRequest;
import com.insurance.backend.web.dto.broker.BrokerDetailsResponse;
import com.insurance.backend.web.dto.broker.BrokerUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.BrokerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokerServiceTest {

    private static final long BROKER_ID = 10L;

    private static final String CODE_RAW = "  BR-001  ";
    private static final String CODE_TRIM = "BR-001";

    private static final String NAME = "Best Broker";
    private static final String EMAIL = "broker@test.ro";
    private static final String PHONE = "0722222222";
    private static final BigDecimal COMM = new BigDecimal("0.10");

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerMapper brokerMapper;

    @InjectMocks
    private BrokerService brokerService;

    @Test
    void createShouldThrowConflictWhenCodeExists() {
        BrokerCreateRequest req = new BrokerCreateRequest(CODE_RAW, NAME, EMAIL, PHONE, COMM);

        when(brokerRepository.existsByBrokerCode(CODE_TRIM)).thenReturn(true);

        assertThrows(ConflictException.class, () -> brokerService.create(req));

        verify(brokerRepository).existsByBrokerCode(CODE_TRIM);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoInteractions(brokerMapper);
    }

    @Test
    void createShouldSaveAndReturnDetailsWhenOk() {
        BrokerCreateRequest req = new BrokerCreateRequest(CODE_RAW, NAME, EMAIL, PHONE, COMM);

        Broker entity = mock(Broker.class);
        Broker saved = mock(Broker.class);
        BrokerDetailsResponse details = mock(BrokerDetailsResponse.class);

        when(brokerRepository.existsByBrokerCode(CODE_TRIM)).thenReturn(false);
        when(brokerMapper.toEntity(req)).thenReturn(entity);
        when(brokerRepository.save(entity)).thenReturn(saved);
        when(brokerMapper.toDetails(saved)).thenReturn(details);

        BrokerDetailsResponse result = brokerService.create(req);

        assertSame(details, result);

        verify(brokerRepository).existsByBrokerCode(CODE_TRIM);
        verify(brokerMapper).toEntity(req);
        verify(brokerRepository).save(entity);
        verify(brokerMapper).toDetails(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenMissing() {
        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> brokerService.getById(BROKER_ID));

        verify(brokerRepository).findById(BROKER_ID);
        verifyNoInteractions(brokerMapper);
    }

    @Test
    void updateShouldApplyUpdateSaveAndReturnDetails() {
        BrokerUpdateRequest req = new BrokerUpdateRequest("New", "new@test.ro", "0700000000", BrokerStatus.INACTIVE, COMM);

        Broker broker = mock(Broker.class);
        Broker saved = mock(Broker.class);
        BrokerDetailsResponse details = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(brokerRepository.save(broker)).thenReturn(saved);
        when(brokerMapper.toDetails(saved)).thenReturn(details);

        BrokerDetailsResponse result = brokerService.update(BROKER_ID, req);

        assertSame(details, result);

        verify(brokerRepository).findById(BROKER_ID);
        verify(brokerMapper).applyUpdate(broker, req);
        verify(brokerRepository).save(broker);
        verify(brokerMapper).toDetails(saved);
    }

    @Test
    void activateShouldSetActiveAndSave() {
        Broker broker = mock(Broker.class);
        Broker saved = mock(Broker.class);
        BrokerDetailsResponse details = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(brokerRepository.save(broker)).thenReturn(saved);
        when(brokerMapper.toDetails(saved)).thenReturn(details);

        BrokerDetailsResponse result = brokerService.activate(BROKER_ID);

        assertSame(details, result);

        verify(brokerRepository).findById(BROKER_ID);
        verify(broker).setStatus(BrokerStatus.ACTIVE);
        verify(brokerRepository).save(broker);
        verify(brokerMapper).toDetails(saved);
    }

    @Test
    void deactivateShouldSetInactiveAndSave() {
        Broker broker = mock(Broker.class);
        Broker saved = mock(Broker.class);
        BrokerDetailsResponse details = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findById(BROKER_ID)).thenReturn(Optional.of(broker));
        when(brokerRepository.save(broker)).thenReturn(saved);
        when(brokerMapper.toDetails(saved)).thenReturn(details);

        BrokerDetailsResponse result = brokerService.deactivate(BROKER_ID);

        assertSame(details, result);

        verify(brokerRepository).findById(BROKER_ID);
        verify(broker).setStatus(BrokerStatus.INACTIVE);
        verify(brokerRepository).save(broker);
        verify(brokerMapper).toDetails(saved);
    }

    @Test
    void searchByBrokerCodeShouldReturnSinglePageWhenFound() {
        Pageable pageable = PageRequest.of(0, 20);
        String code = "  BR-001 ";

        Broker broker = mock(Broker.class);
        BrokerDetailsResponse details = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findByBrokerCode("BR-001")).thenReturn(Optional.of(broker));
        when(brokerMapper.toDetails(broker)).thenReturn(details);

        Page<BrokerDetailsResponse> result = brokerService.search(null, code, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(details, result.getContent().get(0));

        verify(brokerRepository).findByBrokerCode("BR-001");
        verifyNoMoreInteractions(brokerRepository);
    }

    @Test
    void searchByBrokerCodeShouldThrowNotFoundWhenMissing() {
        Pageable pageable = PageRequest.of(0, 20);
        String code = "NOPE";

        when(brokerRepository.findByBrokerCode(code)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> brokerService.search(null, code, pageable));

        verify(brokerRepository).findByBrokerCode(code);
        verifyNoInteractions(brokerMapper);
    }

    @Test
    void searchByNameShouldCallRepoFindByNameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 20);

        Broker b1 = mock(Broker.class);
        BrokerDetailsResponse d1 = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findByNameContainingIgnoreCase("best", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(b1), pageable, 1));
        when(brokerMapper.toDetails(b1)).thenReturn(d1);

        Page<BrokerDetailsResponse> result = brokerService.search("  best  ", null, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(d1, result.getContent().get(0));

        verify(brokerRepository).findByNameContainingIgnoreCase("best", pageable);
    }

    @Test
    void searchNoFiltersShouldCallFindAll() {
        Pageable pageable = PageRequest.of(0, 20);

        Broker b1 = mock(Broker.class);
        BrokerDetailsResponse d1 = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(b1), pageable, 1));
        when(brokerMapper.toDetails(b1)).thenReturn(d1);

        Page<BrokerDetailsResponse> result = brokerService.search(null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(d1, result.getContent().get(0));

        verify(brokerRepository).findAll(pageable);
    }

    @Test
    void searchBrokerCodeBlankShouldFallbackToName() {
        Pageable pageable = PageRequest.of(0, 20);

        Broker b1 = mock(Broker.class);
        BrokerDetailsResponse d1 = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findByNameContainingIgnoreCase("best", pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(b1), pageable, 1));
        when(brokerMapper.toDetails(b1)).thenReturn(d1);

        Page<BrokerDetailsResponse> result = brokerService.search("  best  ", "   ", pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(d1, result.getContent().getFirst());

        verify(brokerRepository).findByNameContainingIgnoreCase("best", pageable);
        verify(brokerRepository, never()).findByBrokerCode(anyString());
        verify(brokerRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchBlankFiltersShouldFallbackToFindAll() {
        Pageable pageable = PageRequest.of(0, 20);

        Broker b1 = mock(Broker.class);
        BrokerDetailsResponse d1 = mock(BrokerDetailsResponse.class);

        when(brokerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(b1), pageable, 1));
        when(brokerMapper.toDetails(b1)).thenReturn(d1);

        Page<BrokerDetailsResponse> result = brokerService.search("   ", "   ", pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(d1, result.getContent().getFirst());

        verify(brokerRepository).findAll(pageable);
        verify(brokerRepository, never()).findByBrokerCode(anyString());
        verify(brokerRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

}
