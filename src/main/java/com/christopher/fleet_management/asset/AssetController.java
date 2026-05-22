package com.christopher.fleet_management.asset;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

	private final AssetService assetService;

	@PostMapping
	@PreAuthorize("hasRole('FLEET_MANAGER')")
	public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO assetDTO) {
		return ResponseEntity.status(201).body(assetService.createAsset(assetDTO));
	}

	@GetMapping
	@PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('OPERATOR')")
	public ResponseEntity<Page<AssetDTO>> getAllAssets(
			@RequestParam(required = false) AssetStatus status,
			@RequestParam(required = false) AssetType type,
			Pageable pageable) {
		return ResponseEntity.ok(assetService.getAllAssets(status, type, pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('OPERATOR')")
	public ResponseEntity<AssetDTO> getAssetById(@PathVariable Long id) {
		return ResponseEntity.ok(assetService.getAssetById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('FLEET_MANAGER')")
	public ResponseEntity<AssetDTO> updateAsset(
			@PathVariable Long id,
			@Valid @RequestBody AssetDTO assetDTO) {
		return ResponseEntity.ok(assetService.updateAsset(id, assetDTO));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('FLEET_MANAGER')")
	public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
		assetService.deleteAsset(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/assign")
	@PreAuthorize("hasRole('FLEET_MANAGER')")
	public ResponseEntity<AssetDTO> assignAsset(
			@PathVariable Long id,
			@RequestParam Long operatorId) {
		return ResponseEntity.ok(assetService.assignAsset(id, operatorId));
	}
}
