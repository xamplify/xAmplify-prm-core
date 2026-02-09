package com.xtremand.user.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.dao.util.GenericDAO;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDAO userDAO;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void registerPrmAccount_passwordMismatch() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        dto.setEmailId("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPassword("Abc123!");
        dto.setConfirmPassword("Abc123@");
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(400, response.getStatusCode());
        verify(userDAO, never()).getUserByEmail(anyString());
    }

    @Test
    public void registerPrmAccount_missingFields() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(400, response.getStatusCode());
        assertEquals("Validation failed", response.getMessage());
        Map<String, String> errors = response.getErrors();
        assertEquals("Email is required", errors.get("emailId"));
        assertEquals("First name is required", errors.get("firstName"));
        assertEquals("Password is required", errors.get("password"));
        assertEquals("Confirm password is required", errors.get("confirmPassword"));
    }

    @Test
    public void registerPrmAccount_success() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        dto.setEmailId("test@example.com");
        dto.setFirstName("Test");
        dto.setPassword("Abc123!");
        dto.setConfirmPassword("Abc123!");
        when(userDAO.getUserByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("Abc123!")).thenReturn("enc");
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(200, response.getStatusCode());
        verify(genericDAO).save(any(User.class));
    }

    @Test
    public void registerPrmAccount_invalidPassword() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        dto.setEmailId("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPassword("pass1");
        dto.setConfirmPassword("pass1");
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(400, response.getStatusCode());
        verify(userDAO, never()).getUserByEmail(anyString());
    }

    @Test
    public void registerPrmAccount_invalidEmail() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        dto.setEmailId("invalid-email");
        dto.setFirstName("Test");
        dto.setPassword("Abc123!");
        dto.setConfirmPassword("Abc123!");
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(400, response.getStatusCode());
        assertEquals("The entered email is not formatted properly", response.getMessage());
        verify(userDAO, never()).getUserByEmail(anyString());
    }

    @Test
    public void registerPrmAccount_prmAccountExists() {
        SignUpRequestDTO dto = new SignUpRequestDTO();
        dto.setEmailId("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPassword("Abc123!");
        dto.setConfirmPassword("Abc123!");
        when(userDAO.prmAccountExists()).thenReturn(true);
        XtremandResponse response = userService.registerPrmAccount(dto);
        assertEquals(400, response.getStatusCode());
        verify(genericDAO, never()).save(any(User.class));
        verify(userDAO, never()).getUserByEmail(anyString());
    }
}
