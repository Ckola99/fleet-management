package com.christopher.fleet_management.asset;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {

	Page<Asset> findByActiveTrue(Pageable pageable);
	Page<Asset> findByActiveTrueAndStatus(AssetStatus status, Pageable pageable);
	Page<Asset> findByActiveTrueAndType(AssetType type, Pageable pageable);
	Page<Asset> findByActiveTrueAndStatusAndType(AssetStatus status, AssetType type, Pageable pageable);
	List<Asset> findByOperatorIdAndActiveTrue(Long operatorId);
}
