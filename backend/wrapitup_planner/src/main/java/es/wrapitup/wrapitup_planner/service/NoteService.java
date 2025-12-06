package es.wrapitup.wrapitup_planner.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.NoteMapper;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, NoteMapper noteMapper, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(UserModel user) {
        return user != null && user.getRoles() != null && user.getRoles().contains("ADMIN");
    }

    public Optional<NoteDTO> findById(Long id) {
        return noteRepository.findById(id)
                               .map(noteMapper::toDto);
    }

    public NoteDTO createNote(NoteDTO noteDTO, String username) {
        
        if (noteDTO.getTitle() == null || noteDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        // admins cannot create notes
        if (isAdmin(user)) {
            throw new IllegalArgumentException("Admins cannot create notes");
        }
        
        
        Note note = new Note(
            user,
            noteDTO.getTitle(),
            noteDTO.getOverview() != null ? noteDTO.getOverview() : "",
            noteDTO.getSummary() != null ? noteDTO.getSummary() : "",
            noteDTO.getJsonQuestions() != null ? noteDTO.getJsonQuestions() : "",
            noteDTO.getVisibility() != null ? noteDTO.getVisibility() : NoteVisibility.PRIVATE
        );
        
        
        if (noteDTO.getCategory() != null) {
            note.setCategory(noteDTO.getCategory());
        }
        
        Note saved = noteRepository.save(note);
        return noteMapper.toDto(saved);
    }

    public List<NoteDTO> findUsersNotesById (String username) {
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return null;
        }
    
        Long userLong = currentUserOpt.get().getId();
        return noteRepository.findByUserId(userLong)
                                .stream()
                                .map(noteMapper::toDto)
                                .collect(Collectors.toList());
    }

    public Page<NoteDTO> findUsersNotesPaginated(String username, Pageable pageable) {
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return null;
        }
        Long userLong = currentUserOpt.get().getId();
        return noteRepository.findByUserId(userLong, pageable)
                                .map(noteMapper::toDto);
    }
    
    public Page<NoteDTO> findRecentNotesByUser(String username, Pageable pageable, String category, String search) {
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return Page.empty();
        }
        Long userId = currentUserOpt.get().getId();
        
        Page<Note> notes;
        
        
        boolean hasCategory = category != null && !category.isEmpty();
        boolean hasSearch = search != null && !search.isEmpty();
        
        if (hasCategory && hasSearch) {
            // Both filters
            NoteCategory noteCategory = NoteCategory.valueOf(category.toUpperCase());
            notes = noteRepository.findByUserIdAndCategoryAndTitleContainingOrderByLastModifiedDesc(userId, noteCategory, search, pageable);
        } else if (hasCategory) {
            // Category only
            NoteCategory noteCategory = NoteCategory.valueOf(category.toUpperCase());
            notes = noteRepository.findByUserIdAndCategoryOrderByLastModifiedDesc(userId, noteCategory, pageable);
        } else if (hasSearch) {
            // Search only
            notes = noteRepository.findByUserIdAndTitleContainingOrderByLastModifiedDesc(userId, search, pageable);
        } else {
            // No filters
            notes = noteRepository.findByUserIdOrderByLastModifiedDesc(userId, pageable);
        }
        
        return notes.map(noteMapper::toDto);
    }

    public Optional<NoteDTO> findByIdWithPermissions(Long id, String username) {
        Optional<Note> noteOpt = noteRepository.findById(id);
        
        if (noteOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Note note = noteOpt.get();
        
        
        if (note.getVisibility() == NoteVisibility.PUBLIC) {
            return Optional.of(noteMapper.toDto(note));
        }
        
        
        if (username == null) {
            return Optional.empty(); 
        }
        
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return Optional.empty();
        }
        
        UserModel currentUser = currentUserOpt.get();
        
        
        if (isAdmin(currentUser)) {
            return Optional.of(noteMapper.toDto(note));
        }
        
        
        if (note.getUser().getId().equals(currentUser.getId())) {
            return Optional.of(noteMapper.toDto(note));
        }
        
        
        boolean isShared = note.getSharedWith().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));
        
        if (isShared) {
            return Optional.of(noteMapper.toDto(note));
        }
        
        return Optional.empty(); 
    }

    public Optional<NoteDTO> updateNote(Long id, NoteDTO noteDTO, String username) {
        Optional<Note> existingNote = noteRepository.findById(id);
        
        if (existingNote.isEmpty()) {
            return Optional.empty();
        }
        
        Note note = existingNote.get();
        
        
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return Optional.empty();
        }
        
        UserModel currentUser = currentUserOpt.get();
        
        // Admins CANNOT edit notes, only owners can
        if (isAdmin(currentUser) || !note.getUser().getId().equals(currentUser.getId())) {
            return Optional.empty(); 
        }
        
        
        if (noteDTO.getTitle() != null) {
            if (noteDTO.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            note.setTitle(noteDTO.getTitle());
        }
        if (noteDTO.getOverview() != null) {
            note.setOverview(noteDTO.getOverview());
        }
        if (noteDTO.getSummary() != null) {
            note.setSummary(noteDTO.getSummary());
        }
        if (noteDTO.getVisibility() != null) {
            note.setVisibility(noteDTO.getVisibility());
        }
        if (noteDTO.getJsonQuestions() != null) {
            note.setJsonQuestions(noteDTO.getJsonQuestions());
        }
        if (noteDTO.getCategory() != null) {
            note.setCategory(noteDTO.getCategory());
        }
        
        // Update lastModified timestamp
        note.setLastModified(LocalDateTime.now());
        
        Note updated = noteRepository.save(note);
        return Optional.of(noteMapper.toDto(updated));
    }

    public Optional<NoteDTO> shareNoteWithUsername(Long noteId, String username, String ownerUsername) {
        Optional<Note> existingNote = noteRepository.findById(noteId);
        
        if (existingNote.isEmpty()) {
            return Optional.empty();
        }
        
        
        if (username.equals(ownerUsername)) {
            return Optional.empty();
        }
        
        Optional<UserModel> userToShare = userRepository.findByUsername(username);
        if (userToShare.isEmpty()) {
            return Optional.empty();
        }
        
        Note note = existingNote.get();
        Set<UserModel> sharedUsers = new HashSet<>(note.getSharedWith());
        sharedUsers.add(userToShare.get());
        
        note.setSharedWith(sharedUsers);
        Note updated = noteRepository.save(note);
        return Optional.of(noteMapper.toDto(updated));
    }

    public boolean deleteNote(Long noteId, String username) {
        Optional<Note> existingNote = noteRepository.findById(noteId);
        
        if (existingNote.isEmpty()) {
            return false;
        }
        
        Note note = existingNote.get();
        
        
        Optional<UserModel> currentUserOpt = userRepository.findByUsername(username);
        if (currentUserOpt.isEmpty()) {
            return false;
        }
        
        UserModel currentUser = currentUserOpt.get();
        
        if (!isAdmin(currentUser) && !note.getUser().getId().equals(currentUser.getId())) {
            return false; 
        }
        
        noteRepository.delete(note);
        return true;
    }
}

