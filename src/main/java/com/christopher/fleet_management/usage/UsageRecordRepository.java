package com.christopher.fleet_management.usage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

	Page<UsageRecord> findByAssetId(Long assetId, Pageable pageable);
}
