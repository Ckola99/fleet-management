package com.christopher.fleet_management.asset;

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
public class AssetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String managerToken;
	private String operatorToken;

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
	}

	// helper to create an asset and return its id
	private Long createAsset(String name, String type, String status) throws Exception {
		AssetDTO dto = AssetDTO.builder()
				.name(name)
				.type(AssetType.valueOf(type))
				.status(AssetStatus.valueOf(status))
				.build();

		MvcResult result = mockMvc.perform(post("/api/assets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andReturn();

		return objectMapper.readTree(
				result.getResponse().getContentAsString()).get("id").asLong();
	}

	@Test
	void shouldCreateAssetAsFleetManager() throws Exception {
		AssetDTO dto = AssetDTO.builder()
				.name("Truck 01")
				.type(AssetType.VEHICLE)
				.status(AssetStatus.ACTIVE)
				.build();

		mockMvc.perform(post("/api/assets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Truck 01"))
				.andExpect(jsonPath("$.type").value("VEHICLE"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void shouldFailCreateAssetAsOperator() throws Exception {
		AssetDTO dto = AssetDTO.builder()
				.name("Truck 01")
				.type(AssetType.VEHICLE)
				.status(AssetStatus.ACTIVE)
				.build();

		mockMvc.perform(post("/api/assets")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + operatorToken)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldFailCreateAssetWithNoToken() throws Exception {
		AssetDTO dto = AssetDTO.builder()
				.name("Truck 01")
				.type(AssetType.VEHICLE)
				.status(AssetStatus.ACTIVE)
				.build();

		mockMvc.perform(post("/api/assets")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldGetAllAssets() throws Exception {
		createAsset("Truck 01", "VEHICLE", "ACTIVE");
		createAsset("Forklift 01", "EQUIPMENT", "ACTIVE");

		mockMvc.perform(get("/api/assets")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.content.length()").value(2));
	}

	@Test
	void shouldGetAllAssetsFilteredByStatus() throws Exception {
		createAsset("Truck 01", "VEHICLE", "ACTIVE");
		createAsset("Truck 02", "VEHICLE", "INACTIVE");

		mockMvc.perform(get("/api/assets?status=ACTIVE")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
	}

	@Test
	void shouldGetAllAssetsFilteredByType() throws Exception {
		createAsset("Truck 01", "VEHICLE", "ACTIVE");
		createAsset("Forklift 01", "EQUIPMENT", "ACTIVE");

		mockMvc.perform(get("/api/assets?type=VEHICLE")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].type").value("VEHICLE"));
	}

	@Test
	void shouldGetAssetById() throws Exception {
		Long id = createAsset("Truck 01", "VEHICLE", "ACTIVE");

		mockMvc.perform(get("/api/assets/" + id)
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Truck 01"));
	}

	@Test
	void shouldReturn404ForNonExistentAsset() throws Exception {
		mockMvc.perform(get("/api/assets/999")
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldUpdateAsset() throws Exception {
		Long id = createAsset("Truck 01", "VEHICLE", "ACTIVE");

		AssetDTO update = AssetDTO.builder()
				.name("Truck 01 Updated")
				.type(AssetType.VEHICLE)
				.status(AssetStatus.UNDER_MAINTENANCE)
				.build();

		mockMvc.perform(put("/api/assets/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + managerToken)
				.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Truck 01 Updated"))
				.andExpect(jsonPath("$.status").value("UNDER_MAINTENANCE"));
	}

	@Test
	void shouldFailUpdateAssetAsOperator() throws Exception {
		Long id = createAsset("Truck 01", "VEHICLE", "ACTIVE");

		AssetDTO update = AssetDTO.builder()
				.name("Truck 01 Updated")
				.type(AssetType.VEHICLE)
				.status(AssetStatus.UNDER_MAINTENANCE)
				.build();

		mockMvc.perform(put("/api/assets/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + operatorToken)
				.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isForbidden());
	}

	@Test
	void shouldSoftDeleteAsset() throws Exception {
		Long id = createAsset("Truck 01", "VEHICLE", "ACTIVE");

		mockMvc.perform(delete("/api/assets/" + id)
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isNoContent());

		// confirm it's gone from the list
		mockMvc.perform(get("/api/assets/" + id)
				.header("Authorization", "Bearer " + managerToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldFailDeleteAssetAsOperator() throws Exception {
		Long id = createAsset("Truck 01", "VEHICLE", "ACTIVE");

		mockMvc.perform(delete("/api/assets/" + id)
				.header("Authorization", "Bearer " + operatorToken))
				.andExpect(status().isForbidden());
	}
}
