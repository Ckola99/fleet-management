package com.christopher.fleet_management.usage;

import org.springframework.stereotype.Component;

@Component
public class UsageRecordMapper {

	public UsageRecordDTO toDTO(UsageRecord record) {
		return UsageRecordDTO.builder()
				.id(record.getId())
				.assetId(record.getAsset().getId())
				.assetName(record.getAsset().getName())
				.hours(record.getHours())
				.mileage(record.getMileage())
				.notes(record.getNotes())
				.timestamp(record.getTimestamp())
				.build();
	}
}
