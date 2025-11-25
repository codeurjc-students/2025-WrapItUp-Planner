package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse;
import es.wrapitup.wrapitup_planner.security.jwt.JwtTokenProvider;
import es.wrapitup.wrapitup_planner.security.jwt.LoginRequest;
import es.wrapitup.wrapitup_planner.security.jwt.UserLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Tag("unit") 
public class AuthServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserLoginService userLoginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSuccessAddsCookiesAndReturnsSuccess() {
        String username = "unituser";
        String password = "unitpass";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        UserDetails userDetails = new User(username, "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        HttpServletResponse response = mock(HttpServletResponse.class);

        var resp = userLoginService.login(response, new LoginRequest(username, password));

        assertEquals(AuthResponse.Status.SUCCESS, resp.getBody().getStatus());
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void loginFailureThrowsWhenAuthenticationFails() {
        String username = "baduser";
        String password = "badpass";

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad creds"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThrows(BadCredentialsException.class, () -> {
            userLoginService.login(response, new LoginRequest(username, password));
        });
    }

    @Test
    void refreshTokenReturnsFailureResponse() {
        String invalidRefreshToken = "invalid-token";

        when(jwtTokenProvider.validateToken(invalidRefreshToken))
            .thenThrow(new RuntimeException("Invalid token"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        var resp = userLoginService.refresh(response, invalidRefreshToken);

        assertEquals(AuthResponse.Status.FAILURE, resp.getBody().getStatus());
        assertTrue(resp.getBody().getMessage().contains("Failure"));
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void logoutClearsCookies() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        String result = userLoginService.logout(response);

        assertEquals("logout successfully", result);
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    void loginsetsSecurity() {
        String username = "contextuser";
        String password = "password";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        UserDetails userDetails = new User(username, "pwd", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        HttpServletResponse response = mock(HttpServletResponse.class);

        userLoginService.login(response, new LoginRequest(username, password));

        verify(authenticationManager).authenticate(any());
        verify(userDetailsService).loadUserByUsername(username);
    }
}
