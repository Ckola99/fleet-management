package com.christopher.fleet_management.usage;

import java.time.LocalDateTime;

import com.christopher.fleet_management.asset.Asset;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UsageRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "asset_id")
	@NotNull(message = "Asset is required")
	private Asset asset;

	@PositiveOrZero(message = "Hours must be zero or positive")
	private Double hours;

	@PositiveOrZero(message = "Mileage must be zero or positive")
	private Double mileage;

	private String notes;

	private LocalDateTime timestamp;
}
