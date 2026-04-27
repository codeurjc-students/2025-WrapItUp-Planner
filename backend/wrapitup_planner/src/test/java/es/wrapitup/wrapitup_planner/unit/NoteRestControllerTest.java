package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import es.wrapitup.wrapitup_planner.controller.NoteRestController;
import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.QuizResultDTO;
import es.wrapitup.wrapitup_planner.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;

@Tag("unit")
public class NoteRestControllerTest {

    private NoteRestController controller;

    @Mock
    private NoteService noteService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new NoteRestController(noteService);
    }

    @Test
    void getSharedNotes_unauthenticated_and_forbidden() {
        when(request.getUserPrincipal()).thenReturn(null);
        var resp = controller.getSharedNotes(0,10,null, request);
        assertEquals(401, resp.getStatusCodeValue());

        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("u");
        when(noteService.findNotesSharedWithUser(eq("u"), any(), any())).thenThrow(new SecurityException("Admins do not have shared notes"));

        var resp2 = controller.getSharedNotes(0,10,null, request);
        assertEquals(403, resp2.getStatusCodeValue());
    }

    @Test
    void submitQuizResult_unauthenticated_and_badrequest() {
        when(request.getUserPrincipal()).thenReturn(null);
        var resp = controller.submitQuizResult(1L, new QuizResultDTO(), request);
        assertEquals(401, resp.getStatusCodeValue());

        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("u");
        when(noteService.saveQuizResult(eq(1L), eq("u"), any())).thenThrow(new IllegalArgumentException("invalid"));

        var resp2 = controller.submitQuizResult(1L, new QuizResultDTO(), request);
        assertEquals(400, resp2.getStatusCodeValue());
    }

    @Test
    void generateQuestionsWithAi_unauthenticated_returns401() {
        when(request.getUserPrincipal()).thenReturn(null);
        var file = new org.springframework.mock.web.MockMultipartFile("file", "f.txt", "text/plain", "c".getBytes());

        var resp = controller.generateQuestionsWithAi(1L, file, request);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void generateQuestionsWithAi_noteNotFound_returns404() {
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("u");
        var file = new org.springframework.mock.web.MockMultipartFile("file", "f.txt", "text/plain", "c".getBytes());

        when(noteService.generateQuizForNoteFromAi(eq(1L), any(), eq("u"))).thenReturn(Optional.empty());

        var resp = controller.generateQuestionsWithAi(1L, file, request);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void generateQuestionsWithAi_forbidden_onSecurityException_returns403() {
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("u");
        var file = new org.springframework.mock.web.MockMultipartFile("file", "f.txt", "text/plain", "c".getBytes());

        when(noteService.generateQuizForNoteFromAi(eq(1L), any(), eq("u"))).thenThrow(new SecurityException("nope"));

        var resp = controller.generateQuestionsWithAi(1L, file, request);
        assertEquals(403, resp.getStatusCodeValue());
    }

    @Test
    void generateQuestionsWithAi_badGateway_onIllegalState_returns502() {
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("u");
        var file = new org.springframework.mock.web.MockMultipartFile("file", "f.txt", "text/plain", "c".getBytes());

        when(noteService.generateQuizForNoteFromAi(eq(1L), any(), eq("u"))).thenThrow(new IllegalStateException("ai fail"));

        var resp = controller.generateQuestionsWithAi(1L, file, request);
        assertEquals(502, resp.getStatusCodeValue());
    }
}
