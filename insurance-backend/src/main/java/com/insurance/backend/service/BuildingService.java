package com.insurance.backend.service;

import com.insurance.backend.domain.building.Building;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingService {

    private static final Logger log = LoggerFactory.getLogger(BuildingService.class);

    private final BuildingRepository buildingRepository;
    private final ClientRepository clientRepository;
    private final CityRepository cityRepository;
    private final BuildingMapper buildingMapper;

    public BuildingService(BuildingRepository buildingRepository, ClientRepository clientRepository,
                           CityRepository cityRepository, BuildingMapper buildingMapper) {
        this.buildingRepository = buildingRepository;
        this.clientRepository = clientRepository;
        this.cityRepository = cityRepository;
        this.buildingMapper = buildingMapper;
    }

    @Transactional
    public BuildingDetailsResponse createForClient(Long clientId, BuildingCreateRequest request) {
        Client owner = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found: " + clientId));

        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new NotFoundException("City not found: " + request.cityId()));

        Building building = buildingMapper.toEntity(request, owner, city);
        Building saved = buildingRepository.save(building);

        log.info("Building created: id={}, clientId={}", saved.getId(), clientId);
        return buildingMapper.toDetails(saved);
    }

    @Transactional(readOnly = true)
    public Page<BuildingDetailsResponse> listForClient(Long clientId, Pageable pageable) {

        if (!clientRepository.existsById(clientId)) {
            throw new NotFoundException("Client not found: " + clientId);
        }
        return buildingRepository.findByOwnerId(clientId, pageable).map(buildingMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public BuildingDetailsResponse getById(Long buildingId) {
        Building b = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new NotFoundException("Building not found: " + buildingId));

        return buildingMapper.toDetails(b);
    }

    @Transactional
    public BuildingDetailsResponse update(Long buildingId, BuildingUpdateRequest request) {
        Building b = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new NotFoundException("Building not found: " + buildingId));

        if (request.clientId() != null && !request.clientId().equals(b.getOwner().getId())) {
            throw new ConflictException("Changing building owner is not allowed");
        }

        City newCity = null;
        if (request.cityId() != null) {
            newCity = cityRepository.findById(request.cityId())
                    .orElseThrow(() -> new NotFoundException("City not found: " + request.cityId()));
        }

        buildingMapper.applyUpdate(b, request, newCity);
        Building saved = buildingRepository.save(b);

        log.info("Building updated: id={}", saved.getId());
        return buildingMapper.toDetails(saved);
    }
}
