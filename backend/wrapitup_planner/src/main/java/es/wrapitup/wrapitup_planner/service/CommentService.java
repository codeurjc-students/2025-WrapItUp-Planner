package es.wrapitup.wrapitup_planner.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.wrapitup.wrapitup_planner.dto.CommentDTO;
import es.wrapitup.wrapitup_planner.dto.CommentMapper;
import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;

@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    
    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, 
                         NoteRepository noteRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }
    
    public Optional<CommentDTO> findById(Long id) {
        return commentRepository.findById(id)
                                .map(commentMapper::toDto);
    }
    
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO, String username) {
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        
        if (commentDTO.getNoteId() == null) {
            throw new IllegalArgumentException("Note ID is required");
        }
        
        Optional<Note> noteOpt = noteRepository.findById(commentDTO.getNoteId());
        if (noteOpt.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        Note note = noteOpt.get();
        UserModel user = userOpt.get();
        
        Comment comment = new Comment(commentDTO.getContent(), note, user);
        Comment saved = commentRepository.save(comment);
        
        return commentMapper.toDto(saved);
    }
    
    public List<CommentDTO> getCommentsByNoteId(Long noteId) {
        return commentRepository.findByNoteIdOrderByCreatedAtDesc(noteId)
                                .stream()
                                .map(commentMapper::toDto)
                                .collect(Collectors.toList());
    }
    
    public Page<CommentDTO> getCommentsByNoteIdPaginated(Long noteId, Pageable pageable) {
        return commentRepository.findByNoteId(noteId, pageable)
                                .map(commentMapper::toDto);
    }
    
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("Comment not found");
        }
        
        Comment comment = commentOpt.get();
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        UserModel user = userOpt.get();
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the comment owner can delete their comment");
        }
        
        commentRepository.deleteById(commentId);
    }
    
    public boolean canUserAccessComments(Long noteId, String username) {
        Optional<Note> noteOpt = noteRepository.findById(noteId);
        if (noteOpt.isEmpty()) {
            return false;
        }
        
        Note note = noteOpt.get();
        
        if (note.getVisibility() == NoteVisibility.PUBLIC) {
            return true;
        }
        
        if (username == null) {
            return false;
        }
        
        Optional<UserModel> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        UserModel user = userOpt.get();
        
        if (note.getUser().getId().equals(user.getId())) {
            return true;
        }
        
        return note.getSharedWith().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));
    }
}
