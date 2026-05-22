package com.christopher.fleet_management.operator;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.christopher.fleet_management.asset.AssetRepository;
import com.christopher.fleet_management.asset.AssetDTO;
import com.christopher.fleet_management.asset.AssetMapper;
import com.christopher.fleet_management.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OperatorService {

	private final OperatorRepository operatorRepository;
	private final AssetRepository assetRepository;
	private final AssetMapper assetMapper;
	private final OperatorMapper operatorMapper;

	public OperatorDTO createOperator(OperatorDTO dto) {
		Operator operator = operatorMapper.toEntity(dto);
		return operatorMapper.toDTO(operatorRepository.save(operator));
	}

	public List<AssetDTO> getAssetsByOperator(Long operatorId) {
		if (!operatorRepository.existsById(operatorId)) {
			throw new ResourceNotFoundException("Operator with id " + operatorId + " not found");
		}
		return assetRepository.findByOperatorIdAndActiveTrue(operatorId)
				.stream()
				.map(assetMapper::toDTO)
				.collect(Collectors.toList());
	}
}
