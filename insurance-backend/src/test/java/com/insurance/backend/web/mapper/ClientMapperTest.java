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

    private static final long CLIENT_ID = 10L;

    private static final String NAME_RAW = "  Johnny Test";
    private static final String NAME_TRIM = "Johnny Test";

    private static final String ID_RAW = "  ID-123  ";
    private static final String ID_TRIM = "ID-123";

    private static final String EMAIL = "johnny@test.com";
    private static final String PHONE = "0700000000";
    private static final String ADDRESS = "Address";

    private static final String OLD_NAME = "Old Name";
    private static final String NEW_NAME_RAW = "  New Name  ";
    private static final String NEW_NAME_TRIM = "New Name";

    private static final String OLD_EMAIL = "old@mail.com";
    private static final String NEW_PHONE = "0711111111";
    private static final String OLD_PHONE = "0700";
    private static final String OLD_ADDRESS = "Old address";

    private static final String IDENTIFICATION_1 = "ID-1";
    private static final String IDENTIFICATION_IGNORED = "ID-IGNORED";

    private static final String SUMMARY_NAME = "Johnny";

    private static final String COMPANY_NAME = "Company";
    private static final String COMPANY_ID = "C-1";
    private static final String COMPANY_EMAIL = "c@c.com";
    private static final String COMPANY_PHONE = "0700";
    private static final String COMPANY_ADDRESS = "Addr";

    private final BuildingMapper buildingMapper = mock(BuildingMapper.class);
    private final ClientMapper clientMapper = new ClientMapper(buildingMapper);

    @Test
    void toEntityShouldTrimNameAndIdentificationNumber() {
        ClientCreateRequest req = new ClientCreateRequest(
                ClientType.INDIVIDUAL,
                NAME_RAW,
                ID_RAW,
                EMAIL,
                PHONE,
                ADDRESS
        );

        Client result = clientMapper.toEntity(req);

        assertNotNull(result);
        assertEquals(ClientType.INDIVIDUAL, result.getClientType());
        assertEquals(NAME_TRIM, result.getName());
        assertEquals(ID_TRIM, result.getIdentificationNumber());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(PHONE, result.getPhone());
        assertEquals(ADDRESS, result.getAddress());
    }

    @Test
    void applyUpdateShouldUpdateOnlyNonNullFieldsAndTrimName() {
        Client client = new Client(
                ClientType.COMPANY,
                OLD_NAME,
                IDENTIFICATION_1,
                OLD_EMAIL,
                OLD_PHONE,
                OLD_ADDRESS
        );

        ClientUpdateRequest req = new ClientUpdateRequest(
                NEW_NAME_RAW,
                null,
                NEW_PHONE,
                null,
                IDENTIFICATION_IGNORED
        );

        clientMapper.applyUpdate(client, req);

        assertEquals(NEW_NAME_TRIM, client.getName());
        assertEquals(OLD_EMAIL, client.getEmail());
        assertEquals(NEW_PHONE, client.getPhone());
        assertEquals(OLD_ADDRESS, client.getAddress());
        assertEquals(IDENTIFICATION_1, client.getIdentificationNumber());
    }

    @Test
    void toSummaryShouldMapFields() {
        Client client = mock(Client.class);

        when(client.getId()).thenReturn(CLIENT_ID);
        when(client.getClientType()).thenReturn(ClientType.INDIVIDUAL);
        when(client.getName()).thenReturn(SUMMARY_NAME);
        when(client.getIdentificationNumber()).thenReturn(ID_TRIM);

        ClientSummaryResponse result = clientMapper.toSummary(client);

        assertNotNull(result);
        assertEquals(CLIENT_ID, result.id());
        assertEquals(ClientType.INDIVIDUAL, result.clientType());
        assertEquals(SUMMARY_NAME, result.name());
        assertEquals(ID_TRIM, result.identificationNumber());
    }

    @Test
    void toDetailsShouldMapFieldsAndMapBuildingsWithBuildingMapper() {
        Client client = mock(Client.class);

        Building b1 = mock(Building.class);
        Building b2 = mock(Building.class);

        when(client.getId()).thenReturn(CLIENT_ID);
        when(client.getClientType()).thenReturn(ClientType.COMPANY);
        when(client.getName()).thenReturn(COMPANY_NAME);
        when(client.getIdentificationNumber()).thenReturn(COMPANY_ID);
        when(client.getEmail()).thenReturn(COMPANY_EMAIL);
        when(client.getPhone()).thenReturn(COMPANY_PHONE);
        when(client.getAddress()).thenReturn(COMPANY_ADDRESS);
        when(client.getBuildings()).thenReturn(List.of(b1, b2));

        BuildingDetailsResponse d1 = mock(BuildingDetailsResponse.class);
        BuildingDetailsResponse d2 = mock(BuildingDetailsResponse.class);

        when(buildingMapper.toDetails(b1)).thenReturn(d1);
        when(buildingMapper.toDetails(b2)).thenReturn(d2);

        ClientDetailsResponse result = clientMapper.toDetails(client);

        assertNotNull(result);
        assertEquals(CLIENT_ID, result.id());
        assertEquals(ClientType.COMPANY, result.clientType());
        assertEquals(COMPANY_NAME, result.name());
        assertEquals(COMPANY_ID, result.identificationNumber());
        assertEquals(COMPANY_EMAIL, result.email());
        assertEquals(COMPANY_PHONE, result.phone());
        assertEquals(COMPANY_ADDRESS, result.address());

        assertNotNull(result.buildings());
        assertEquals(2, result.buildings().size());
        assertSame(d1, result.buildings().get(0));
        assertSame(d2, result.buildings().get(1));

        verify(buildingMapper).toDetails(b1);
        verify(buildingMapper).toDetails(b2);
    }

    @Test
    void applyUpdateAllNullDoesNotChangeAnything() {
        Client client = new Client(
                ClientType.COMPANY,
                OLD_NAME,
                IDENTIFICATION_1,
                OLD_EMAIL,
                OLD_PHONE,
                OLD_ADDRESS
        );

        ClientUpdateRequest req = new ClientUpdateRequest(
                null,
                null,
                null,
                null,
                null
        );

        clientMapper.applyUpdate(client, req);

        assertEquals(OLD_NAME, client.getName());
        assertEquals(OLD_EMAIL, client.getEmail());
        assertEquals(OLD_PHONE, client.getPhone());
        assertEquals(OLD_ADDRESS, client.getAddress());
        assertEquals(IDENTIFICATION_1, client.getIdentificationNumber());
    }
}
