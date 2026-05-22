package com.christopher.fleet_management.usage;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.christopher.fleet_management.asset.Asset;
import com.christopher.fleet_management.asset.AssetRepository;
import com.christopher.fleet_management.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsageRecordService {

	private final UsageRecordRepository usageRecordRepository;
	private final AssetRepository assetRepository;
	private final UsageRecordMapper mapper;

	public UsageRecordDTO logUsage(Long assetId, UsageRecordDTO dto) {
		Asset asset = assetRepository.findById(assetId)
				.filter(Asset::isActive)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Asset with id " + assetId + " not found"));

		UsageRecord record = UsageRecord.builder()
				.asset(asset)
				.hours(dto.getHours())
				.mileage(dto.getMileage())
				.notes(dto.getNotes())
				.timestamp(LocalDateTime.now())
				.build();

		return mapper.toDTO(usageRecordRepository.save(record));
	}

	public Page<UsageRecordDTO> getUsageHistory(Long assetId, Pageable pageable) {
		if (!assetRepository.existsById(assetId)) {
			throw new ResourceNotFoundException("Asset with id " + assetId + " not found");
		}
		return usageRecordRepository.findByAssetId(assetId, pageable)
				.map(mapper::toDTO);
	}
}
