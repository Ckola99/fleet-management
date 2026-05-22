package com.christopher.fleet_management.operator;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.christopher.fleet_management.asset.AssetDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
public class OperatorController {

	private final OperatorService operatorService;

	@GetMapping("/{id}/assets")
	@PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('OPERATOR')")
	public ResponseEntity<List<AssetDTO>> getAssetsByOperator(@PathVariable Long id) {
		return ResponseEntity.ok(operatorService.getAssetsByOperator(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('FLEET_MANAGER')")
	public ResponseEntity<OperatorDTO> createOperator(@Valid @RequestBody OperatorDTO dto) {
    		return ResponseEntity.status(201).body(operatorService.createOperator(dto));
	}
}
