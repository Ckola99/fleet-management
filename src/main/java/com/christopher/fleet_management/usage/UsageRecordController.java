package com.christopher.fleet_management.usage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class UsageRecordController {

	private final UsageRecordService usageRecordService;

	@PostMapping("/{id}/usage")
	@PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('OPERATOR')")
	public ResponseEntity<UsageRecordDTO> logUsage(
			@PathVariable Long id,
			@Valid @RequestBody UsageRecordDTO dto) {
		return ResponseEntity.status(201).body(usageRecordService.logUsage(id, dto));
	}

	@GetMapping("/{id}/usage")
	@PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('OPERATOR')")
	public ResponseEntity<Page<UsageRecordDTO>> getUsageHistory(
			@PathVariable Long id,
			Pageable pageable) {
		return ResponseEntity.ok(usageRecordService.getUsageHistory(id, pageable));
	}
}
