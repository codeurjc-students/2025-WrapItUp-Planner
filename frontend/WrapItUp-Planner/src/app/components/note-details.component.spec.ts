import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { convertToParamMap, ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoteDetailComponent } from './notes-details.component';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { CommentService } from '../services/comment.service';
import { NoteDTO } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { CommentDTO } from '../dtos/comment.dto';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('NoteDetailComponent', () => {
  let component: NoteDetailComponent;
  let fixture: ComponentFixture<NoteDetailComponent>;
  let mockNoteService: jasmine.SpyObj<NoteService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockCommentService: jasmine.SpyObj<CommentService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const testNote: NoteDTO = {
    id: 1,
    title: 'Test Note Title',
    overview: 'Resumen general de la sesión',
    summary: 'Este es el contenido detallado del resumen',
    jsonQuestions: '{"questions": ["¿Qué es esto?", "¿Cómo funciona?"]}',
    visibility: 'PUBLIC',
    userId: 1,
    sharedWithUserIds: []
  };

  const testUser: UserModelDTO = {
    id: 1,
    username: 'testuser',
    email: 'test@test.com',
    password: ''
  };

  const testComments: CommentDTO[] = [
    {
      id: 1,
      content: 'First comment',
      noteId: 1,
      username: 'testuser',
      displayName: 'Test User',
      createdAt: '2025-01-01T10:00:00'
    },
    {
      id: 2,
      content: 'Second comment',
      noteId: 1,
      username: 'otheruser',
      displayName: 'Other User',
      createdAt: '2025-01-01T11:00:00'
    }
  ];

  const testCommentsPage = {
    content: testComments,
    totalElements: 2,
    totalPages: 1,
    last: true,
    first: true,
    size: 10,
    number: 0
  };

  beforeEach(async () => {
    mockNoteService = jasmine.createSpyObj('NoteService', ['getNoteById', 'updateNote', 'shareNoteByUsername', 'deleteNote']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockCommentService = jasmine.createSpyObj('CommentService', ['getCommentsByNote', 'createComment', 'deleteComment', 'reportComment']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [NoteDetailComponent],
      imports: [
        FormsModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'login', component: NoteDetailComponent },
          { path: 'profile', component: NoteDetailComponent }
        ])
      ],
      providers: [
        { provide: NoteService, useValue: mockNoteService },
        { provide: UserService, useValue: mockUserService },
        { provide: CommentService, useValue: mockCommentService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '1' }))
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(NoteDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch note from route param and display it', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockNoteService.getNoteById).toHaveBeenCalledWith(1);
    expect(component.note).toEqual(testNote);
  });

  it('should set canEdit to true when current user is the note owner', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.canEdit).toBe(true);
    expect(component.canShare).toBe(true);
  });

  it('should set canEdit to false when current user is not the note owner', () => {
    const otherUser: UserModelDTO = { ...testUser, id: 2, username: 'otheruser' };
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(otherUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.canEdit).toBe(false);
    expect(component.canShare).toBe(false);
  });

  it('should toggle edit mode when toggleEditMode is called', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.editMode).toBe(false);
    
    component.toggleEditMode();
    expect(component.editMode).toBe(true);
    
    component.toggleEditMode();
    expect(component.editMode).toBe(false);
  });

  it('should update note when saveChanges is called', () => {
    const updatedNote: NoteDTO = { ...testNote, title: 'Updated Title' };
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.updateNote.and.returnValue(of(updatedNote));

    component.ngOnInit();
    fixture.detectChanges();

    component.editedTitle = 'Updated Title';
    component.saveChanges();

    expect(mockNoteService.updateNote).toHaveBeenCalledWith(1, jasmine.objectContaining({
      title: 'Updated Title'
    }));
  });

  it('should open share modal when openShareModal is called', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    component.openShareModal();

    expect(component.showShareModal).toBe(true);
    expect(component.shareUsername).toBe('');
  });

  it('should block share modal when user cannot share', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of({ ...testUser, id: 2 }));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.openShareModal();

    expect(window.alert).toHaveBeenCalledWith('You do not have permission to share this note');
    expect(component.showShareModal).toBe(false);
  });

  it('should share note with username', () => {
    const sharedNote: NoteDTO = { ...testNote, sharedWithUserIds: [2] };
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.shareNoteByUsername.and.returnValue(of(sharedNote));

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = 'otheruser';
    component.shareWithUsername();

    expect(mockNoteService.shareNoteByUsername).toHaveBeenCalledWith(1, 'otheruser');
  });

  it('should show error when sharing note with yourself', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = 'testuser';
    component.shareWithUsername();

    expect(component.shareError).toBe('You cannot share a note with yourself');
    expect(mockNoteService.shareNoteByUsername).not.toHaveBeenCalled();
  });

  it('should require username when sharing note', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = '   ';
    component.shareWithUsername();

    expect(component.shareError).toBe('Please enter a username');
    expect(mockNoteService.shareNoteByUsername).not.toHaveBeenCalled();
  });

  it('should set error when sharing note with unknown user', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.shareNoteByUsername.and.returnValue(throwError(() => ({ status: 404 })));

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = 'missing';
    component.shareWithUsername();

    expect(component.shareError).toBe('User not found');
  });

  it('should set generic error when sharing note fails', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.shareNoteByUsername.and.returnValue(throwError(() => ({ status: 400 })));

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = 'otheruser';
    component.shareWithUsername();

    expect(component.shareError).toBe('Error sharing note');
  });

  it('should delete note when deleteNote is called', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.deleteNote.and.returnValue(of(void 0));
    spyOn(window, 'confirm').and.returnValue(true);

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(mockNoteService.deleteNote).toHaveBeenCalledWith(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/my-notes']);
  });

  it('should block delete when user cannot delete note', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of({ ...testUser, id: 2, roles: [] }));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(window.alert).toHaveBeenCalledWith('You do not have permission to delete this note');
    expect(mockNoteService.deleteNote).not.toHaveBeenCalled();
  });

  it('should route to error on delete failure (server)', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.deleteNote.and.returnValue(throwError(() => ({ status: 500 })));
    spyOn(window, 'confirm').and.returnValue(true);

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should show error message on delete failure (client)', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.deleteNote.and.returnValue(throwError(() => ({ status: 400 })));
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(window.alert).toHaveBeenCalledWith('Error deleting note');
  });

  it('should not delete note when user cancels confirmation', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    spyOn(window, 'confirm').and.returnValue(false);

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(mockNoteService.deleteNote).not.toHaveBeenCalled();
  });

  it('should return shared usernames count', () => {
    mockNoteService.getNoteById.and.returnValue(of({ ...testNote, sharedWithUserIds: [2, 3] }));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    const result = component.getSharedUsernames();
    expect(result).toBe('Shared with 2 users');
  });

  it('should return not shared when shared list is empty', () => {
    mockNoteService.getNoteById.and.returnValue(of({ ...testNote, sharedWithUserIds: [] }));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.getSharedUsernames()).toBe('Not shared');
  });

  it('should use singular label for one shared user', () => {
    mockNoteService.getNoteById.and.returnValue(of({ ...testNote, sharedWithUserIds: [2] }));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.getSharedUsernames()).toBe('Shared with 1 user');
  });

  it('should block edit mode when user cannot edit', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of({ ...testUser, id: 2 }));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.toggleEditMode();

    expect(window.alert).toHaveBeenCalledWith('You do not have permission to edit this note');
    expect(component.editMode).toBe(false);
  });

  it('should require title when saving changes', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.editedTitle = '   ';
    component.saveChanges();

    expect(window.alert).toHaveBeenCalledWith('Title cannot be empty');
    expect(mockNoteService.updateNote).not.toHaveBeenCalled();
  });

  it('should route to error on update failure (server)', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.updateNote.and.returnValue(throwError(() => ({ status: 500 })));

    component.ngOnInit();
    fixture.detectChanges();

    component.editedTitle = 'Updated Title';
    component.saveChanges();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should show server message on update failure', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    mockNoteService.updateNote.and.returnValue(throwError(() => ({ status: 400, error: { message: 'Bad update' } })));
    spyOn(window, 'alert');

    component.ngOnInit();
    fixture.detectChanges();

    component.editedTitle = 'Updated Title';
    component.saveChanges();

    expect(window.alert).toHaveBeenCalledWith('Bad update');
  });

  it('should handle getProfilePicUrl without url', () => {
    expect(component.getProfilePicUrl({ id: 1, content: 'x', noteId: 1, username: 'u', displayName: 'd', createdAt: '2025-01-01T10:00:00' })).toBe('');
  });

  it('should handle getProfilePicUrl with url', () => {
    const comment = { id: 1, content: 'x', noteId: 1, username: 'u', displayName: 'd', createdAt: '2025-01-01T10:00:00', userProfilePicUrl: '/img.png' } as CommentDTO;
    expect(component.getProfilePicUrl(comment)).toBe('https://localhost/img.png');
  });

  // Comment Tests
  describe('Comments functionality', () => {
    
    beforeEach(() => {
      mockNoteService.getNoteById.and.returnValue(of(testNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
    });

    it('should load comments when note is fetched', () => {
      component.ngOnInit();
      fixture.detectChanges();

      expect(mockCommentService.getCommentsByNote).toHaveBeenCalledWith(1, 0, 10);
      expect(component.comments).toEqual(testComments);
      expect(component.totalComments).toBe(2);
    });

    it('should create a new comment and reload comments', () => {
      const newComment: CommentDTO = {
        id: 3,
        content: 'New comment',
        noteId: 1,
        username: 'testuser',
        displayName: 'Test User',
        createdAt: '2025-01-01T13:00:00'
      };

      component.ngOnInit();
      fixture.detectChanges();

      mockCommentService.createComment.and.returnValue(of(newComment));
      mockCommentService.getCommentsByNote.calls.reset();
      mockCommentService.getCommentsByNote.and.returnValue(of({
        ...testCommentsPage,
        content: [newComment, ...testComments],
        totalElements: 3
      }));

      component.newCommentContent = 'New comment';
      component.addComment();

      expect(mockCommentService.createComment).toHaveBeenCalledWith(1, { content: 'New comment' });
      expect(component.newCommentContent).toBe('');
      expect(mockCommentService.getCommentsByNote).toHaveBeenCalledWith(1, 0, 10);
    });

    it('should not create empty comment', () => {
      component.ngOnInit();
      fixture.detectChanges();

      component.newCommentContent = '   ';
      component.addComment();

      expect(mockCommentService.createComment).not.toHaveBeenCalled();
    });

    it('should alert on comment create unauthorized', () => {
      spyOn(window, 'alert');
      mockCommentService.createComment.and.returnValue(throwError(() => ({ status: 401 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.newCommentContent = 'New comment';
      component.addComment();

      expect(window.alert).toHaveBeenCalledWith('You must log in to comment');
    });

    it('should alert on comment create forbidden', () => {
      spyOn(window, 'alert');
      mockCommentService.createComment.and.returnValue(throwError(() => ({ status: 403 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.newCommentContent = 'New comment';
      component.addComment();

      expect(window.alert).toHaveBeenCalledWith('You do not have permission to comment on this note');
    });

    it('should route to error on comment create server error', () => {
      mockCommentService.createComment.and.returnValue(throwError(() => ({ status: 500 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.newCommentContent = 'New comment';
      component.addComment();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should delete a comment and reload comments', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      
      component.ngOnInit();
      fixture.detectChanges();

      mockCommentService.deleteComment.and.returnValue(of(void 0));
      mockCommentService.getCommentsByNote.calls.reset();
      mockCommentService.getCommentsByNote.and.returnValue(of({
        ...testCommentsPage,
        content: [testComments[1]],
        totalElements: 1
      }));

      component.deleteComment(1);

      expect(mockCommentService.deleteComment).toHaveBeenCalledWith(1, 1);
      expect(mockCommentService.getCommentsByNote).toHaveBeenCalledWith(1, 0, 10);
    });

    it('should not delete comment when confirmation is cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);

      component.ngOnInit();
      fixture.detectChanges();

      component.deleteComment(1);

      expect(mockCommentService.deleteComment).not.toHaveBeenCalled();
    });

    it('should handle delete comment error', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(window, 'alert');
      mockCommentService.deleteComment.and.returnValue(throwError(() => ({ status: 400 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.deleteComment(1);

      expect(window.alert).toHaveBeenCalledWith('Error deleting comment');
    });

    it('should route to error on delete comment server error', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockCommentService.deleteComment.and.returnValue(throwError(() => ({ status: 500 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.deleteComment(1);

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should load more comments with pagination', () => {
      component.currentPage = 0;
      component.ngOnInit();
      fixture.detectChanges();

      const newPage = {
        content: [],
        totalElements: 2,
        totalPages: 2,
        last: true,
        first: false,
        size: 10,
        number: 1
      };
      mockCommentService.getCommentsByNote.and.returnValue(of(newPage));

      component.loadMoreComments();

      expect(component.currentPage).toBe(1);
      expect(mockCommentService.getCommentsByNote).toHaveBeenCalledWith(1, 1, 10);
    });

    it('should allow user to delete only their own comments', () => {
      component.currentUser = testUser;
      const userComment: CommentDTO = { ...testComments[0], username: 'testuser' };
      const otherComment: CommentDTO = { ...testComments[1], username: 'otheruser' };

      expect(component.canDeleteComment(userComment)).toBe(true);
      expect(component.canDeleteComment(otherComment)).toBe(false);
    });

    it('should allow admin to delete any comment', () => {
      component.currentUser = { ...testUser, roles: ['ADMIN'] };
      const otherComment: CommentDTO = { ...testComments[1], username: 'otheruser' };

      expect(component.canDeleteComment(otherComment)).toBe(true);
    });

    it('should not report comment when confirmation is cancelled', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      component.reportComment(1);

      expect(mockCommentService.reportComment).not.toHaveBeenCalled();
    });

    it('should report comment and reload comments', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      component.ngOnInit();
      fixture.detectChanges();

      mockCommentService.reportComment.and.returnValue(of(testComments[0]));
      const reloadSpy = spyOn(component, 'loadComments').and.callThrough();

      component.reportComment(1);

      expect(mockCommentService.reportComment).toHaveBeenCalledWith(1, 1);
      expect(reloadSpy).toHaveBeenCalledWith(true);
    });

    it('should handle report comment unauthorized', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(window, 'alert');
      mockCommentService.reportComment.and.returnValue(throwError(() => ({ status: 401 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.reportComment(1);

      expect(window.alert).toHaveBeenCalledWith('You must log in to report a comment');
    });

    it('should route to error on report comment server error', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockCommentService.reportComment.and.returnValue(throwError(() => ({ status: 500 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.reportComment(1);

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });
  });

  it('should handle loadCurrentUser error and clear permissions', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 500 })));
    mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.canEdit).toBe(false);
    expect(component.canShare).toBe(false);
  });

  it('should handle fetchNote 401 error', () => {
    mockNoteService.getNoteById.and.returnValue(throwError(() => ({ status: 401 })));
    spyOn(window, 'alert');

    component.noteId = 1;
    component.fetchNote();

    expect(window.alert).toHaveBeenCalledWith('You must log in to view this note');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should handle fetchNote 403 error', () => {
    mockNoteService.getNoteById.and.returnValue(throwError(() => ({ status: 403 })));
    spyOn(window, 'alert');

    component.noteId = 1;
    component.fetchNote();

    expect(window.alert).toHaveBeenCalledWith('You do not have permission to view this note');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/my-notes']);
  });

  it('should handle fetchNote 404 error', () => {
    mockNoteService.getNoteById.and.returnValue(throwError(() => ({ status: 404 })));
    spyOn(window, 'alert');

    component.noteId = 1;
    component.fetchNote();

    expect(window.alert).toHaveBeenCalledWith('Note not found');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/my-notes']);
  });

  it('should handle fetchNote unexpected error', () => {
    mockNoteService.getNoteById.and.returnValue(throwError(() => ({ status: 400 })));

    component.noteId = 1;
    component.fetchNote();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should handle loadComments server error', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockCommentService.getCommentsByNote.and.returnValue(throwError(() => ({ status: 500 })));

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    expect(component.loadingComments).toBe(false);
  });

  it('should toggle comment menu', () => {
    component.toggleCommentMenu(1);
    expect(component.showCommentMenu).toBe(1);
    component.toggleCommentMenu(1);
    expect(component.showCommentMenu).toBeNull();
  });
});