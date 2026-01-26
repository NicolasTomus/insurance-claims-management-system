package com.insurance.backend.service;

import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.infrastructure.persistence.repository.ClientRepository;
import com.insurance.backend.web.dto.client.ClientCreateRequest;
import com.insurance.backend.web.dto.client.ClientDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.client.ClientUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.ClientMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void create_shouldThrowConflict_whenIdentificationNumberAlreadyExists() {
        ClientCreateRequest req = new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                "Johnny Test",
                "ID-123",
                "johnny@test.com",
                "0700000000",
                "Address"
        );

        when(clientRepository.existsByIdentificationNumber("ID-123")).thenReturn(true);

        assertThrows(ConflictException.class, () -> clientService.create(req));

        verify(clientRepository).existsByIdentificationNumber("ID-123");
        verifyNoMoreInteractions(clientRepository);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void create_shouldSaveAndReturnDetails_whenOk() {
        ClientCreateRequest req = new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                "Johnny Test",
                "ID-123",
                "johnny@test.com",
                "0700000000",
                "Address"
        );

        Client entity = mock(Client.class);
        Client saved = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.existsByIdentificationNumber("ID-123")).thenReturn(false);
        when(clientMapper.toEntity(req)).thenReturn(entity);
        when(clientRepository.save(entity)).thenReturn(saved);
        when(clientMapper.toDetails(saved)).thenReturn(dto);

        ClientDetailsResponse result = clientService.create(req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientRepository).existsByIdentificationNumber("ID-123");
        verify(clientRepository).save(entity);
        verify(clientMapper).toEntity(req);
        verify(clientMapper).toDetails(saved);
    }

    @Test
    void getById_shouldThrowNotFound_whenClientMissing() {
        when(clientRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getById(10L));

        verify(clientRepository).findById(10L);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void getById_shouldReturnDetails_whenClientExists() {
        Client client = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(clientMapper.toDetails(client)).thenReturn(dto);

        ClientDetailsResponse result = clientService.getById(10L);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientRepository).findById(10L);
        verify(clientMapper).toDetails(client);
    }

    @Test
    void update_shouldThrowNotFound_whenClientMissing() {
        when(clientRepository.findById(10L)).thenReturn(Optional.empty());

        ClientUpdateRequest req = new ClientUpdateRequest(
                "New Name",
                "new@email.com",
                "0711111111",
                "New Address",
                null
        );

        assertThrows(NotFoundException.class, () -> clientService.update(10L, req));

        verify(clientRepository).findById(10L);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void update_shouldThrowConflict_whenIdentificationNumberChanges() {
        Client client = mock(Client.class);

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(client.getIdentificationNumber()).thenReturn("ID-OLD");

        ClientUpdateRequest req = new ClientUpdateRequest(
                "New Name",
                "new@email.com",
                "0711111111",
                "New Address",
                "ID-NEW"
        );

        assertThrows(ConflictException.class, () -> clientService.update(10L, req));

        verify(clientRepository).findById(10L);
        verify(client, atLeastOnce()).getIdentificationNumber();
        verify(clientRepository, never()).save(any());
        verify(clientMapper, never()).applyUpdate(any(), any());
    }

    @Test
    void update_shouldUpdateAndReturnDetails_whenOk() {
        Client client = mock(Client.class);
        Client saved = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.findById(10L)).thenReturn(Optional.of(client));
        when(client.getIdentificationNumber()).thenReturn("ID-123");
        when(clientRepository.save(client)).thenReturn(saved);
        when(clientMapper.toDetails(saved)).thenReturn(dto);

        ClientUpdateRequest req = new ClientUpdateRequest(
                "New Name",
                "new@email.com",
                "0711111111",
                "New Address",
                "ID-123"
        );

        ClientDetailsResponse result = clientService.update(10L, req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientMapper).applyUpdate(client, req);
        verify(clientRepository).save(client);
        verify(clientMapper).toDetails(saved);
    }

    @Test
    void search_shouldSearchByIdentifier_andThrowNotFound_whenNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        when(clientRepository.findByIdentificationNumber("ID-123"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.search(null, "ID-123", pageable));

        verify(clientRepository).findByIdentificationNumber("ID-123");
        verify(clientRepository, never()).findByNameContainingIgnoreCase(anyString(), any());
        verify(clientRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void search_shouldSearchByIdentifier_andReturnOneItem_whenFound() {
        Pageable pageable = PageRequest.of(0, 10);

        Client client = mock(Client.class);
        ClientSummaryResponse summary = mock(ClientSummaryResponse.class);

        when(clientRepository.findByIdentificationNumber("ID-123")).thenReturn(Optional.of(client));
        when(clientMapper.toSummary(client)).thenReturn(summary);

        Page<ClientSummaryResponse> result = clientService.search(null, "ID-123", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(summary, result.getContent().getFirst());

        verify(clientRepository).findByIdentificationNumber("ID-123");
        verify(clientMapper).toSummary(client);
    }

    @Test
    void search_shouldSearchByName_whenNameProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        Client c1 = mock(Client.class);
        Client c2 = mock(Client.class);

        ClientSummaryResponse s1 = mock(ClientSummaryResponse.class);
        ClientSummaryResponse s2 = mock(ClientSummaryResponse.class);

        when(clientRepository.findByNameContainingIgnoreCase("johnny", pageable))
                .thenReturn(new PageImpl<>(List.of(c1, c2), pageable, 2));
        when(clientMapper.toSummary(c1)).thenReturn(s1);
        when(clientMapper.toSummary(c2)).thenReturn(s2);

        Page<ClientSummaryResponse> result = clientService.search("johnny", "", pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(clientRepository).findByNameContainingIgnoreCase("johnny", pageable);
        verify(clientMapper).toSummary(c1);
        verify(clientMapper).toSummary(c2);
    }

    @Test
    void search_shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        Client c1 = mock(Client.class);
        ClientSummaryResponse s1 = mock(ClientSummaryResponse.class);

        when(clientRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(c1), pageable, 1));
        when(clientMapper.toSummary(c1)).thenReturn(s1);

        Page<ClientSummaryResponse> result = clientService.search(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(s1, result.getContent().getFirst());

        verify(clientRepository).findAll(pageable);
        verify(clientMapper).toSummary(c1);
    }
}
