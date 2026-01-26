package com.insurance.backend.web.mapper;

import com.insurance.backend.domain.building.Building;
import com.insurance.backend.domain.client.Client;
import com.insurance.backend.domain.client.ClientType;
import com.insurance.backend.web.dto.building.BuildingDetailsResponse;
import com.insurance.backend.web.dto.client.ClientCreateRequest;
import com.insurance.backend.web.dto.client.ClientDetailsResponse;
import com.insurance.backend.web.dto.client.ClientSummaryResponse;
import com.insurance.backend.web.dto.client.ClientUpdateRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientMapperTest {

    private final BuildingMapper buildingMapper = mock(BuildingMapper.class);
    private final ClientMapper clientMapper = new ClientMapper(buildingMapper);

    @Test
    void toEntity_shouldTrimNameAndIdentificationNumber() {
        ClientCreateRequest req = new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                "  Johnny Test",
                "  ID-123  ",
                "johnny@test.com",
                "0700000000",
                "Address"
        );

        Client result = clientMapper.toEntity(req);

        assertNotNull(result);
        assertEquals(ClientType.INDIVIDUAL, result.getClientType());
        assertEquals("Johnny Test", result.getName());
        assertEquals("ID-123", result.getIdentificationNumber());
        assertEquals("johnny@test.com", result.getEmail());
        assertEquals("0700000000", result.getPhone());
        assertEquals("Address", result.getAddress());
    }

    @Test
    void applyUpdate_shouldUpdateOnlyNonNullFields_andTrimName() {
        Client client = new Client(
                ClientType.COMPANY,
                "Old Name",
                "ID-1",
                "old@mail.com",
                "0700",
                "Old address"
        );

        ClientUpdateRequest req = new ClientUpdateRequest(
                "  New Name  ",
                null,
                "0711111111",
                null,
                "ID-IGNORED"
        );

        clientMapper.applyUpdate(client, req);

        assertEquals("New Name", client.getName());
        assertEquals("old@mail.com", client.getEmail());
        assertEquals("0711111111", client.getPhone());
        assertEquals("Old address", client.getAddress());
        assertEquals("ID-1", client.getIdentificationNumber());
    }

    @Test
    void toSummary_shouldMapFields() {
        Client c = mock(Client.class);

        when(c.getId()).thenReturn(10L);
        when(c.getClientType()).thenReturn(ClientType.INDIVIDUAL);
        when(c.getName()).thenReturn("Johnny");
        when(c.getIdentificationNumber()).thenReturn("ID-123");

        ClientSummaryResponse result = clientMapper.toSummary(c);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals(ClientType.INDIVIDUAL, result.clientType());
        assertEquals("Johnny", result.name());
        assertEquals("ID-123", result.identificationNumber());
    }

    @Test
    void toDetails_shouldMapFields_andMapBuildingsWithBuildingMapper() {
        Client c = mock(Client.class);

        Building b1 = mock(Building.class);
        Building b2 = mock(Building.class);

        when(c.getId()).thenReturn(10L);
        when(c.getClientType()).thenReturn(ClientType.COMPANY);
        when(c.getName()).thenReturn("Company");
        when(c.getIdentificationNumber()).thenReturn("C-1");
        when(c.getEmail()).thenReturn("c@c.com");
        when(c.getPhone()).thenReturn("0700");
        when(c.getAddress()).thenReturn("Addr");
        when(c.getBuildings()).thenReturn(List.of(b1, b2));

        BuildingDetailsResponse d1 = mock(BuildingDetailsResponse.class);
        BuildingDetailsResponse d2 = mock(BuildingDetailsResponse.class);

        when(buildingMapper.toDetails(b1)).thenReturn(d1);
        when(buildingMapper.toDetails(b2)).thenReturn(d2);

        ClientDetailsResponse result = clientMapper.toDetails(c);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals(ClientType.COMPANY, result.clientType());
        assertEquals("Company", result.name());
        assertEquals("C-1", result.identificationNumber());
        assertEquals("c@c.com", result.email());
        assertEquals("0700", result.phone());
        assertEquals("Addr", result.address());

        assertNotNull(result.buildings());
        assertEquals(2, result.buildings().size());
        assertSame(d1, result.buildings().get(0));
        assertSame(d2, result.buildings().get(1));

        verify(buildingMapper).toDetails(b1);
        verify(buildingMapper).toDetails(b2);
    }
}
