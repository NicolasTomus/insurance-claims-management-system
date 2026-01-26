package com.insurance.backend.service;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.geography.City;
import com.insurance.backend.infrastructure.persistence.repository.BuildingRepository;
import com.insurance.backend.infrastructure.persistence.repository.CityRepository;
import com.insurance.backend.infrastructure.persistence.repository.ClientRepository;
import com.insurance.backend.web.dto.building.BuildingCreateRequest;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.building.BuildingUpdateRequest;
import com.insurance.backend.web.exception.ConflictException;
import com.insurance.backend.web.exception.NotFoundException;
import com.insurance.backend.web.mapper.BuildingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private BuildingMapper buildingMapper;

    @InjectMocks
    private BuildingService buildingService;

    @Test
    void createForClient_shouldThrow_whenClientNotFound() {
        Long clientId = 10L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Some address",
                100L,
                2000,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("150000.00"),
                false,
                true
        );

        assertThrows(NotFoundException.class, () -> buildingService.createForClient(clientId, req));

        verify(clientRepository).findById(clientId);
    }

    @Test
    void createForClient_shouldThrow_whenCityNotFound() {
        Long clientId = 10L;
        Long cityId = 100L;

        Client owner = mock(Client.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(owner));
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Some address",
                cityId,
                2000,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("150000.00"),
                false,
                true
        );

        assertThrows(NotFoundException.class, () -> buildingService.createForClient(clientId, req));

        verify(clientRepository).findById(clientId);
        verify(cityRepository).findById(cityId);
    }

    @Test
    void createForClient_shouldCreateBuilding_whenOk() {
        Long clientId = 10L;
        Long cityId = 100L;

        Client owner = mock(Client.class);
        City city = mock(City.class);

        Building entity = mock(Building.class);
        Building saved = mock(Building.class);
        BuildingDetailsResponse dto = mock(BuildingDetailsResponse.class);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(owner));
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));

        BuildingCreateRequest req = new BuildingCreateRequest(
                "Some address",
                cityId,
                2000,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("150000.00"),
                false,
                true
        );

        when(buildingMapper.toEntity(req, owner, city)).thenReturn(entity);
        when(buildingRepository.save(entity)).thenReturn(saved);
        when(buildingMapper.toDetails(saved)).thenReturn(dto);

        BuildingDetailsResponse result = buildingService.createForClient(clientId, req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(buildingRepository).save(entity);
        verify(buildingMapper).toDetails(saved);
    }

    @Test
    void listForClient_shouldThrow_whenClientDoesNotExist() {
        Long clientId = 1L;

        when(clientRepository.existsById(clientId)).thenReturn(false);

        Pageable pageable = PageRequest.of(0, 20);
        assertThrows(NotFoundException.class, () -> buildingService.listForClient(clientId, pageable));

        verify(clientRepository).existsById(clientId);
    }

    @Test
    void listForClient_shouldReturnPage_whenOk() {
        Long clientId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        Building b1 = mock(Building.class);
        Building b2 = mock(Building.class);

        BuildingDetailsResponse d1 = mock(BuildingDetailsResponse.class);
        BuildingDetailsResponse d2 = mock(BuildingDetailsResponse.class);

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(buildingRepository.findByOwnerId(clientId, pageable))
                .thenReturn(new PageImpl<>(List.of(b1, b2), pageable, 2));

        when(buildingMapper.toDetails(b1)).thenReturn(d1);
        when(buildingMapper.toDetails(b2)).thenReturn(d2);

        Page<BuildingDetailsResponse> result = buildingService.listForClient(clientId, pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertSame(d1, result.getContent().get(0));
        assertSame(d2, result.getContent().get(1));
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(buildingRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> buildingService.getById(7L));

        verify(buildingRepository).findById(7L);
    }

    @Test
    void getById_shouldReturnDto_whenFound() {
        Building b = mock(Building.class);
        BuildingDetailsResponse dto = mock(BuildingDetailsResponse.class);

        when(buildingRepository.findById(7L)).thenReturn(Optional.of(b));
        when(buildingMapper.toDetails(b)).thenReturn(dto);

        BuildingDetailsResponse result = buildingService.getById(7L);

        assertNotNull(result);
        assertSame(dto, result);
    }

    @Test
    void update_shouldThrow_whenBuildingNotFound() {
        when(buildingRepository.findById(50L)).thenReturn(Optional.empty());

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                "addr",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(NotFoundException.class, () -> buildingService.update(50L, req));

        verify(buildingRepository).findById(50L);
    }

    @Test
    void update_shouldThrowConflict_whenClientIdIsDifferent() {
        Long buildingId = 50L;

        Building b = mock(Building.class);
        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(1L);
        when(b.getOwner()).thenReturn(owner);

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(b));

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                2L,
                "addr",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> buildingService.update(buildingId, req));

        verify(buildingRepository).findById(buildingId);
    }

    @Test
    void update_shouldThrow_whenCityIdProvidedButCityNotFound() {
        Long buildingId = 50L;
        Long cityId = 123L;

        Building b = mock(Building.class);
        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(1L);
        when(b.getOwner()).thenReturn(owner);

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(b));
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                "addr", cityId,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(NotFoundException.class, () -> buildingService.update(buildingId, req));

        verify(cityRepository).findById(cityId);
    }

    @Test
    void update_shouldUpdate_whenCityIdProvided() {
        Long buildingId = 50L;
        Long cityId = 123L;

        Building b = mock(Building.class);
        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(1L);
        when(b.getOwner()).thenReturn(owner);

        City newCity = mock(City.class);

        Building saved = mock(Building.class);
        BuildingDetailsResponse dto = mock(BuildingDetailsResponse.class);

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(b));
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(newCity));
        when(buildingRepository.save(b)).thenReturn(saved);
        when(buildingMapper.toDetails(saved)).thenReturn(dto);

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                "Some address",
                cityId,
                2000,
                BuildingType.RESIDENTIAL,
                2,
                new BigDecimal("120.50"),
                new BigDecimal("150000.00"),
                Boolean.FALSE,
                Boolean.TRUE
        );

        BuildingDetailsResponse result = buildingService.update(buildingId, req);

        assertNotNull(result);
        assertSame(dto, result);

        verify(buildingMapper).applyUpdate(b, req, newCity);
        verify(buildingRepository).save(b);
    }

    @Test
    void update_shouldUpdate_whenCityIdIsNull() {
        Long buildingId = 50L;

        Building b = mock(Building.class);
        Client owner = mock(Client.class);
        when(owner.getId()).thenReturn(1L);
        when(b.getOwner()).thenReturn(owner);

        Building saved = mock(Building.class);
        BuildingDetailsResponse dto = mock(BuildingDetailsResponse.class);

        when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(b));
        when(buildingRepository.save(b)).thenReturn(saved);
        when(buildingMapper.toDetails(saved)).thenReturn(dto);

        BuildingUpdateRequest req = new BuildingUpdateRequest(
                1L,
                "addr",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        BuildingDetailsResponse result = buildingService.update(buildingId, req);

        assertNotNull(result);
        assertSame(dto, result);

        verifyNoInteractions(cityRepository);

        verify(buildingMapper).applyUpdate(b, req, null);
        verify(buildingRepository).save(b);
    }
}
