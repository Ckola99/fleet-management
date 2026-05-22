package com.christopher.fleet_management.usage;

import java.time.LocalDateTime;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordDTO {
	private Long id;
	private Long assetId;
	private String assetName;

	@PositiveOrZero(message = "Hours must be zero or positive")
	private Double hours;

	@PositiveOrZero(message = "Mileage must be zero or positive")
	private Double mileage;

	private String notes;
	private LocalDateTime timestamp;
}
