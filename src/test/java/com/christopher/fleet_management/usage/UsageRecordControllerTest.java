package com.christopher.fleet_management.usage;

import com.christopher.fleet_management.asset.AssetDTO;
import com.christopher.fleet_management.asset.AssetStatus;
import com.christopher.fleet_management.asset.AssetType;
import com.christopher.fleet_management.auth.AuthenticationRequest;
import com.christopher.fleet_management.auth.RegisterRequest;
import com.christopher.fleet_management.auth.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsageRecordControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String managerToken;
	private String operatorToken;
	private Long assetId;

	@BeforeEach
	void setUp() throws Exception {
		// register and login as fleet manager
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						RegisterRequest.builder()
								.name("Chris Manager")
								.email("chris@fleet.com")
								.password("password123")
								.role(Role.FLEET_MANAGER)
								.build())))
				.andExpect(status().isCreated());

		MvcResult managerLogin = mockMvc.perform(post("/api/v1/auth/authenticate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						AuthenticationRequest.builder()
								.email("chris@fleet.com")
								.password("password123")
								.build())))
				.andReturn();

		managerToken = objectMapper.readTree(
				managerLogin.getResponse().getContentAsString()).get("token").asText();

		// register and login as operator
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						RegisterRequest.builder()
								.name("John Operator")
								.email("john@fleet.com")
								.password("password123")
								.role(Role.OPERATOR)
								.build())))
				.andExpect(status().isCreated());

		MvcResult operatorLogin = mockMvc.perform(post("/api/v1/auth/authenticate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						AuthenticationRequest.builder()
								.email("john@fleet.com")
								.password("password123")
								.build())))
				.andReturn();

		operatorToken = objectMapper.readTree(
				operatorLogin.getResponse().getContentAsString()).get("token").asText();

		// create an asset to use in tests
		MvcResult assetResult = mockMvc.perform(post("/api/assets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(
						AssetDTO.builder()
								.name("Truck 01")
								.type(AssetType.VEHICLE)
								.status(AssetStatus.ACTIVE)
								.build())))
				.andReturn();

		assetId = objectMapper.readTree(
				assetResult.getResponse().getContentAsString()).get("id").asLong();
	}

	@Test
	void shouldLogUsageAsFleetManager() throws Exception {
		UsageRecordDTO dto = UsageRecordDTO.builder()
				.hours(8.5)
				.mileage(120.0)
				.notes("Long haul delivery to Pretoria")
				.build();

		mockMvc.perform(post("/api/assets/" + assetId + "/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.hours").value(8.5))
				.andExpect(jsonPath("$.mileage").value(120.0))
				.andExpect(jsonPath("$.notes").value("Long haul delivery to Pretoria"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void shouldLogUsageAsOperator() throws Exception {
		UsageRecordDTO dto = UsageRecordDTO.builder()
				.hours(4.0)
				.mileage(60.0)
				.notes("Local delivery")
				.build();

		mockMvc.perform(post("/api/assets/" + assetId + "/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + operatorToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.hours").value(4.0));
	}

	@Test
	void shouldFailLogUsageWithNoToken() throws Exception {
		UsageRecordDTO dto = UsageRecordDTO.builder()
				.hours(4.0)
				.mileage(60.0)
				.build();

		mockMvc.perform(post("/api/assets/" + assetId + "/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFailLogUsageForNonExistentAsset() throws Exception {
		UsageRecordDTO dto = UsageRecordDTO.builder()
				.hours(4.0)
				.mileage(60.0)
				.build();

		mockMvc.perform(post("/api/assets/999/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldGetUsageHistory() throws Exception {
		// log two records
		UsageRecordDTO dto1 = UsageRecordDTO.builder()
				.hours(8.5)
				.mileage(120.0)
				.notes("First trip")
				.build();

		UsageRecordDTO dto2 = UsageRecordDTO.builder()
				.hours(4.0)
				.mileage(60.0)
				.notes("Second trip")
				.build();

		mockMvc.perform(post("/api/assets/" + assetId + "/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto1)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/assets/" + assetId + "/usage")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto2)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/assets/" + assetId + "/usage")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()").value(2));
	}

	@Test
	void shouldGetUsageHistoryAsOperator() throws Exception {
		mockMvc.perform(get("/api/assets/" + assetId + "/usage")
				.header("Authorization", "Bearer " + operatorToken))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturn404UsageHistoryForNonExistentAsset() throws Exception {
		mockMvc.perform(get("/api/assets/999/usage")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isNotFound());
	}
}
