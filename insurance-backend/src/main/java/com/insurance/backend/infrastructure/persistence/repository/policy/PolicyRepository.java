package com.insurance.backend.infrastructure.persistence.repository.policy;

import com.insurance.backend.domain.building.BuildingType;
import com.insurance.backend.domain.policy.Policy;
import com.insurance.backend.domain.policy.PolicyStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    interface PolicyAggregateView {
        Long getGroupId();
        String getGroupName();
        String getCurrencyCode();
        long getPolicyCount();
        BigDecimal getTotalFinalPremium();
        BigDecimal getTotalFinalPremiumInBaseCurrency();
    }

    @Query("""
            select p from Policy p
            where (:clientId is null or p.client.id = :clientId)
              and (:brokerId is null or p.broker.id = :brokerId)
              and (:status is null or p.status = :status)
              and (:startDate is null or p.endDate >= :startDate)
              and (:endDate is null or p.startDate <= :endDate)
            """)
    org.springframework.data.domain.Page<Policy> search(Long clientId, Long brokerId, PolicyStatus status, LocalDate startDate, LocalDate endDate, org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Query("""
        update Policy p
        set p.status = 'EXPIRED',
            p.updatedAt = :now
        where p.status = 'ACTIVE'
          and p.endDate < :today
    """)
    int bulkExpire(@Param("today") LocalDate today, @Param("now") Instant now);

    @Query("""
        select
          b.id as groupId,
          b.name as groupName,
          c.code as currencyCode,
          count(p) as policyCount,
          sum(coalesce(p.finalPremiumAmount, 0)) as totalFinalPremium,
          sum(coalesce(p.finalPremiumAmount, 0) * c.exchangeRateToBase) as totalFinalPremiumInBaseCurrency
        from Policy p
        join p.broker b
        join p.currency c
        join p.building bd
        where p.startDate >= coalesce(:from, p.startDate)
          and p.startDate <= coalesce(:to,   p.startDate)
          and (:status is null or p.status = :status)
          and (:currency is null or c.code = :currency)
          and (:buildingType is null or bd.buildingType = :buildingType)
        group by b.id, b.name, c.code
    """)
    List<PolicyAggregateView> aggregateByBroker(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") PolicyStatus status,
            @Param("currency") String currency,
            @Param("buildingType") BuildingType buildingType
    );

    @Query("""
        select
          country.id as groupId,
          country.name as groupName,
          c.code as currencyCode,
          count(p) as policyCount,
          sum(coalesce(p.finalPremiumAmount, 0)) as totalFinalPremium,
          sum(coalesce(p.finalPremiumAmount, 0) * c.exchangeRateToBase) as totalFinalPremiumInBaseCurrency
        from Policy p
        join p.currency c
        join p.building bd
        join bd.city city
        join city.county county
        join county.country country
        where p.startDate >= coalesce(:from, p.startDate)
          and p.startDate <= coalesce(:to,   p.startDate)
          and (:status is null or p.status = :status)
          and (:currency is null or c.code = :currency)
          and (:buildingType is null or bd.buildingType = :buildingType)
        group by country.id, country.name, c.code
    """)
    List<PolicyAggregateView> aggregateByCountry(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") PolicyStatus status,
            @Param("currency") String currency,
            @Param("buildingType") BuildingType buildingType
    );

    @Query("""
        select
          county.id as groupId,
          county.name as groupName,
          c.code as currencyCode,
          count(p) as policyCount,
          sum(coalesce(p.finalPremiumAmount, 0)) as totalFinalPremium,
          sum(coalesce(p.finalPremiumAmount, 0) * c.exchangeRateToBase) as totalFinalPremiumInBaseCurrency
        from Policy p
        join p.currency c
        join p.building bd
        join bd.city city
        join city.county county
        where p.startDate >= coalesce(:from, p.startDate)
          and p.startDate <= coalesce(:to,   p.startDate)
          and (:status is null or p.status = :status)
          and (:currency is null or c.code = :currency)
          and (:buildingType is null or bd.buildingType = :buildingType)
        group by county.id, county.name, c.code
    """)
    List<PolicyAggregateView> aggregateByCounty(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") PolicyStatus status,
            @Param("currency") String currency,
            @Param("buildingType") BuildingType buildingType
    );

    @Query("""
        select
          city.id as groupId,
          city.name as groupName,
          c.code as currencyCode,
          count(p) as policyCount,
          sum(coalesce(p.finalPremiumAmount, 0)) as totalFinalPremium,
          sum(coalesce(p.finalPremiumAmount, 0) * c.exchangeRateToBase) as totalFinalPremiumInBaseCurrency
        from Policy p
        join p.currency c
        join p.building bd
        join bd.city city
        where p.startDate >= coalesce(:from, p.startDate)
          and p.startDate <= coalesce(:to,   p.startDate)
          and (:status is null or p.status = :status)
          and (:currency is null or c.code = :currency)
          and (:buildingType is null or bd.buildingType = :buildingType)
        group by city.id, city.name, c.code
    """)
    List<PolicyAggregateView> aggregateByCity(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") PolicyStatus status,
            @Param("currency") String currency,
            @Param("buildingType") BuildingType buildingType
    );
}