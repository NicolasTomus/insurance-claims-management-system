package com.insurance.backend.service;

import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.infrastructure.persistence.repository.client.ClientRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final long CLIENT_ID = 10L;

    private static final String IDENTIFICATION_NUMBER = "ID-123";
    private static final String NAME = "Johnny Test";
    private static final String EMAIL = "johnny@test.com";
    private static final String PHONE = "0700000000";
    private static final String ADDRESS = "Address";

    private static final String UPDATED_NAME = "New Name";
    private static final String UPDATED_EMAIL = "new@email.com";
    private static final String UPDATED_PHONE = "0711111111";
    private static final String UPDATED_ADDRESS = "New Address";

    private static final String OLD_IDENTIFICATION_NUMBER = "ID-OLD";
    private static final String NEW_IDENTIFICATION_NUMBER = "ID-NEW";

    private static final String SEARCH_NAME = "johnny";


    private static Pageable defaultPageable() {
        return PageRequest.of(0, 10);
    }

    private static ClientCreateRequest createRequest() {
        return new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                NAME,
                IDENTIFICATION_NUMBER,
                EMAIL,
                PHONE,
                ADDRESS
        );
    }

    private static ClientUpdateRequest updateRequest(String identificationNumber) {
        return new ClientUpdateRequest(
                UPDATED_NAME,
                UPDATED_EMAIL,
                UPDATED_PHONE,
                UPDATED_ADDRESS,
                identificationNumber
        );
    }

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createShouldThrowConflictWhenIdentificationNumberAlreadyExists() {
        ClientCreateRequest req = createRequest();

        when(clientRepository.existsByIdentificationNumber(IDENTIFICATION_NUMBER)).thenReturn(true);

        assertThrows(ConflictException.class, () -> clientService.create(req));

        verify(clientRepository).existsByIdentificationNumber(IDENTIFICATION_NUMBER);
        verifyNoMoreInteractions(clientRepository);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void createShouldSaveAndReturnDetailsWhenOk() {
        ClientCreateRequest req = createRequest();

        Client entity = mock(Client.class);
        Client saved = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.existsByIdentificationNumber(IDENTIFICATION_NUMBER)).thenReturn(false);
        when(clientMapper.toEntity(req)).thenReturn(entity);
        when(clientRepository.save(entity)).thenReturn(saved);
        when(clientMapper.toDetails(saved)).thenReturn(dto);

        ClientDetailsResponse result = clientService.create(req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientRepository).existsByIdentificationNumber(IDENTIFICATION_NUMBER);
        verify(clientRepository).save(entity);
        verify(clientMapper).toEntity(req);
        verify(clientMapper).toDetails(saved);
    }

    @Test
    void getByIdShouldThrowNotFoundWhenClientMissing() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.getById(CLIENT_ID));

        verify(clientRepository).findById(CLIENT_ID);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void getByIdShouldReturnDetailsWhenClientExists() {
        Client client = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientMapper.toDetails(client)).thenReturn(dto);

        ClientDetailsResponse result = clientService.getById(CLIENT_ID);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientRepository).findById(CLIENT_ID);
        verify(clientMapper).toDetails(client);
    }

    @Test
    void updateShouldThrowNotFoundWhenClientMissing() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

        ClientUpdateRequest req = updateRequest(null);

        assertThrows(NotFoundException.class, () -> clientService.update(CLIENT_ID, req));

        verify(clientRepository).findById(CLIENT_ID);
        verifyNoInteractions(clientMapper);
    }

    @Test
    void updateShouldThrowConflictWhenIdentificationNumberChanges() {
        Client client = mock(Client.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(client.getIdentificationNumber()).thenReturn(OLD_IDENTIFICATION_NUMBER);

        ClientUpdateRequest req = updateRequest(NEW_IDENTIFICATION_NUMBER);

        assertThrows(ConflictException.class, () -> clientService.update(CLIENT_ID, req));

        verify(clientRepository).findById(CLIENT_ID);
        verify(client, atLeastOnce()).getIdentificationNumber();
        verify(clientRepository, never()).save(any());
        verify(clientMapper, never()).applyUpdate(any(), any());
    }

    @Test
    void updateShouldUpdateAndReturnDetailsWhenOk() {
        Client client = mock(Client.class);
        Client saved = mock(Client.class);
        ClientDetailsResponse dto = mock(ClientDetailsResponse.class);

        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(client.getIdentificationNumber()).thenReturn(IDENTIFICATION_NUMBER);
        when(clientRepository.save(client)).thenReturn(saved);
        when(clientMapper.toDetails(saved)).thenReturn(dto);

        ClientUpdateRequest req = updateRequest(IDENTIFICATION_NUMBER);

        ClientDetailsResponse result = clientService.update(CLIENT_ID, req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(clientMapper).applyUpdate(client, req);
        verify(clientRepository).save(client);
        verify(clientMapper).toDetails(saved);
    }

    @Test
    void searchShouldSearchByIdentifierAndThrowNotFoundWhenNotFound() {
        Pageable pageable = defaultPageable();

        when(clientRepository.findByIdentificationNumber(IDENTIFICATION_NUMBER)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.search(null, IDENTIFICATION_NUMBER, pageable));

        verify(clientRepository).findByIdentificationNumber(IDENTIFICATION_NUMBER);
        verify(clientRepository, never()).findByNameContainingIgnoreCase(anyString(), any());
        verify(clientRepository, never()).findAll(any(Pageable.class));
        verifyNoInteractions(clientMapper);
    }

    @Test
    void searchShouldSearchByIdentifierAndReturnOneItemWhenFound() {
        Pageable pageable = defaultPageable();

        Client client = mock(Client.class);
        ClientSummaryResponse summary = mock(ClientSummaryResponse.class);

        when(clientRepository.findByIdentificationNumber(IDENTIFICATION_NUMBER)).thenReturn(Optional.of(client));
        when(clientMapper.toSummary(client)).thenReturn(summary);

        Page<ClientSummaryResponse> result = clientService.search(null, IDENTIFICATION_NUMBER, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertSame(summary, result.getContent().getFirst());

        verify(clientRepository).findByIdentificationNumber(IDENTIFICATION_NUMBER);
        verify(clientMapper).toSummary(client);
    }

    @Test
    void searchShouldSearchByNameWhenNameProvided() {
        Pageable pageable = defaultPageable();

        Client c1 = mock(Client.class);
        Client c2 = mock(Client.class);

        ClientSummaryResponse s1 = mock(ClientSummaryResponse.class);
        ClientSummaryResponse s2 = mock(ClientSummaryResponse.class);

        when(clientRepository.findByNameContainingIgnoreCase(SEARCH_NAME, pageable))
                .thenReturn(new PageImpl<>(List.of(c1, c2), pageable, 2));
        when(clientMapper.toSummary(c1)).thenReturn(s1);
        when(clientMapper.toSummary(c2)).thenReturn(s2);

        Page<ClientSummaryResponse> result = clientService.search(SEARCH_NAME, "", pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        verify(clientRepository).findByNameContainingIgnoreCase(SEARCH_NAME, pageable);
        verify(clientMapper).toSummary(c1);
        verify(clientMapper).toSummary(c2);
    }

    @Test
    void searchShouldReturnAllWhenNoFilters() {
        Pageable pageable = defaultPageable();

        Client c1 = mock(Client.class);
        ClientSummaryResponse s1 = mock(ClientSummaryResponse.class);

        when(clientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c1), pageable, 1));
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
