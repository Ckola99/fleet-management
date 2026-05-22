package com.christopher.fleet_management.operator;

import org.springframework.stereotype.Component;

@Component
public class OperatorMapper {

	public OperatorDTO toDTO(Operator operator) {
		return OperatorDTO.builder()
				.id(operator.getId())
				.name(operator.getName())
				.email(operator.getEmail())
				.build();
	}

	public Operator toEntity(OperatorDTO dto) {
		return Operator.builder()
				.name(dto.getName())
				.email(dto.getEmail())
				.build();
	}
}
