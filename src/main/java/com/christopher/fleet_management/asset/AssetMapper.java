package com.christopher.fleet_management.asset;

import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

	public AssetDTO toDTO(Asset asset) {
		return AssetDTO.builder()
				.id(asset.getId())
				.name(asset.getName())
				.type(asset.getType())
				.status(asset.getStatus())
				.active(asset.isActive())
				.operatorId(asset.getOperator() != null ? asset.getOperator().getId() : null)
				.operatorName(asset.getOperator() != null ? asset.getOperator().getName() : null)
				.build();
	}

	public Asset toEntity(AssetDTO dto) {
		return Asset.builder()
				.name(dto.getName())
				.type(dto.getType())
				.status(dto.getStatus())
				.active(true)
				.build();
	}
}
