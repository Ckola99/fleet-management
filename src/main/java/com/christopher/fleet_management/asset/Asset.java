package com.christopher.fleet_management.asset;

import com.christopher.fleet_management.operator.Operator;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Asset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Type is required")
	@Enumerated(EnumType.STRING)
	private AssetType type;

	@NotNull(message = "Status is required")
	@Enumerated(EnumType.STRING)
	private AssetStatus status;

	private boolean active = true;

	@ManyToOne
	@JoinColumn(name = "operator_id")
	private Operator operator;
}
