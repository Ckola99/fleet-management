package com.christopher.fleet_management.asset;

import com.christopher.fleet_management.operator.Operator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
	private Long id;
	private String name;
	private AssetType type;
	private AssetStatus status;
	private boolean active;
	private Long operatorId;
	private String operatorName;
}
