package com.christopher.fleet_management.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldRegisterSuccessfully() throws Exception {
		RegisterRequest request = RegisterRequest.builder()
				.name("Chris Manager")
				.email("chris@fleet.com")
				.password("password123")
				.role(Role.FLEET_MANAGER)
				.build();

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	void shouldFailRegisterWithDuplicateEmail() throws Exception {
		RegisterRequest request = RegisterRequest.builder()
				.name("Chris Manager")
				.email("chris@fleet.com")
				.password("password123")
				.role(Role.FLEET_MANAGER)
				.build();

		// register first time
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated());

		// register second time with same email
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldFailRegisterWithMissingFields() throws Exception {
		RegisterRequest request = RegisterRequest.builder()
				.email("chris@fleet.com")
				// missing name, password, role
				.build();

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldAuthenticateSuccessfully() throws Exception {
		// first register
		RegisterRequest registerRequest = RegisterRequest.builder()
				.name("Chris Manager")
				.email("chris@fleet.com")
				.password("password123")
				.role(Role.FLEET_MANAGER)
				.build();

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)))
				.andExpect(status().isCreated());

		// then login
		AuthenticationRequest loginRequest = AuthenticationRequest.builder()
				.email("chris@fleet.com")
				.password("password123")
				.build();

		mockMvc.perform(post("/api/v1/auth/authenticate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	void shouldFailAuthenticationWithWrongPassword() throws Exception {
		// first register
		RegisterRequest registerRequest = RegisterRequest.builder()
				.name("Chris Manager")
				.email("chris@fleet.com")
				.password("password123")
				.role(Role.FLEET_MANAGER)
				.build();

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)))
				.andExpect(status().isCreated());

		// login with wrong password
		AuthenticationRequest loginRequest = AuthenticationRequest.builder()
				.email("chris@fleet.com")
				.password("wrongpassword")
				.build();

		mockMvc.perform(post("/api/v1/auth/authenticate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isUnauthorized());
	}
}
