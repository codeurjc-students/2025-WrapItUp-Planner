package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.NoteMapper;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse;
import es.wrapitup.wrapitup_planner.security.jwt.JwtTokenProvider;
import es.wrapitup.wrapitup_planner.security.jwt.LoginRequest;
import es.wrapitup.wrapitup_planner.security.jwt.UserLoginService;
import es.wrapitup.wrapitup_planner.service.NoteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Tag("unit") 
public class ServiceUnitTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserLoginService userLoginService;

    @InjectMocks
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByIdReturnsDTO() {
        Note note = new Note();
        note.setId(1L);
        note.setOverview("Resumen general de la sesión de IA");
        note.setSummary("Este es el contenido detallado del resumen");
        note.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        note.setVisibility(true);

        NoteDTO dto = new NoteDTO();
        dto.setId(1L);
        dto.setOverview("Resumen general de la sesión de IA");
        dto.setSummary("Este es el contenido detallado del resumen");
        dto.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        dto.setVisibility(true);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteMapper.toDto(note)).thenReturn(dto);

        Optional<NoteDTO> result = noteService.findById(1L);

        assertEquals(true, result.isPresent());
        assertEquals(dto.getId(), result.get().getId());
        assertEquals(dto.getOverview(), result.get().getOverview());
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

}
