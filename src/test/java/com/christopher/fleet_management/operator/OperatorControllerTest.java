package com.christopher.fleet_management.operator;

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
public class OperatorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String managerToken;
	private String operatorToken;
	private Long operatorId;
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

		// create an operator record
		MvcResult operatorResult = mockMvc.perform(post("/api/operators")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(
						OperatorDTO.builder()
								.name("John Operator")
								.email("john@fleet.com")
								.build())))
				.andReturn();

		operatorId = objectMapper.readTree(
				operatorResult.getResponse().getContentAsString()).get("id").asLong();

		// create an asset and assign it to the operator
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

		// assign asset to operator
		mockMvc.perform(post("/api/assets/" + assetId + "/assign?operatorId=" + operatorId)
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk());
	}

	@Test
	void shouldCreateOperator() throws Exception {
		OperatorDTO dto = OperatorDTO.builder()
				.name("Jane Operator")
				.email("jane@fleet.com")
				.build();

		mockMvc.perform(post("/api/operators")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Jane Operator"))
				.andExpect(jsonPath("$.email").value("jane@fleet.com"));
	}

	@Test
	void shouldFailCreateOperatorAsOperator() throws Exception {
		OperatorDTO dto = OperatorDTO.builder()
				.name("Jane Operator")
				.email("jane@fleet.com")
				.build();

		mockMvc.perform(post("/api/operators")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + operatorToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldGetAssetsForOperator() throws Exception {
		mockMvc.perform(get("/api/operators/" + operatorId + "/assets")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Truck 01"));
	}

	@Test
	void shouldGetAssetsForOperatorAsOperator() throws Exception {
		mockMvc.perform(get("/api/operators/" + operatorId + "/assets")
				.header("Authorization", "Bearer " + operatorToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void shouldReturn404ForNonExistentOperator() throws Exception {
		mockMvc.perform(get("/api/operators/999/assets")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFailGetAssetsWithNoToken() throws Exception {
		mockMvc.perform(get("/api/operators/" + operatorId + "/assets"))
				.andExpect(status().isForbidden());
	}
}
