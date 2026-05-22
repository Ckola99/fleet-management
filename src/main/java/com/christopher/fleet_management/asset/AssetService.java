package com.christopher.fleet_management.asset;

import com.christopher.fleet_management.exception.ResourceNotFoundException;
import com.christopher.fleet_management.operator.Operator;
import com.christopher.fleet_management.operator.OperatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetService {

	private final AssetRepository assetRepository;
	private final OperatorRepository operatorRepository;
	private final AssetMapper mapper;

	public AssetDTO createAsset(AssetDTO assetDTO) {
		Asset asset = mapper.toEntity(assetDTO);
		return mapper.toDTO(assetRepository.save(asset));
	}

	public Page<AssetDTO> getAllAssets(AssetStatus status, AssetType type, Pageable pageable) {
		if (status != null && type != null) {
			return assetRepository.findByActiveTrueAndStatusAndType(status, type, pageable)
					.map(mapper::toDTO);
		} else if (status != null) {
			return assetRepository.findByActiveTrueAndStatus(status, pageable)
					.map(mapper::toDTO);
		} else if (type != null) {
			return assetRepository.findByActiveTrueAndType(type, pageable)
					.map(mapper::toDTO);
		}
		return assetRepository.findByActiveTrue(pageable)
				.map(mapper::toDTO);
	}

	public AssetDTO getAssetById(Long id) {
		return assetRepository.findById(id)
				.filter(Asset::isActive)
				.map(mapper::toDTO)
				.orElseThrow(() -> new ResourceNotFoundException("Asset with id " + id + " not found"));
	}

	public AssetDTO updateAsset(Long id, AssetDTO assetDTO) {
		Asset asset = assetRepository.findById(id)
				.filter(Asset::isActive)
				.orElseThrow(() -> new ResourceNotFoundException("Asset with id " + id + " not found"));

		asset.setName(assetDTO.getName());
		asset.setType(assetDTO.getType());
		asset.setStatus(assetDTO.getStatus());

		return mapper.toDTO(assetRepository.save(asset));
	}

	public void deleteAsset(Long id) {
		Asset asset = assetRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asset with id " + id + " not found"));
		asset.setActive(false);
		assetRepository.save(asset);
	}

	public AssetDTO assignAsset(Long assetId, Long operatorId) {
		Asset asset = assetRepository.findById(assetId)
				.filter(Asset::isActive)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Asset with id " + assetId + " not found"));

		Operator operator = operatorRepository.findById(operatorId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Operator with id " + operatorId + " not found"));

		asset.setOperator(operator);
		return mapper.toDTO(assetRepository.save(asset));
	}
}
