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
    mockNoteService = jasmine.createSpyObj('NoteService', [
      'getNoteById',
      'updateNote',
      'shareNoteByUsername',
      'deleteNote',
      'submitQuizResult',
      'generateQuestionsWithAi'
    ]);
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

  describe('Quiz functionality', () => {
    const quizNote: NoteDTO = {
      ...testNote,
      jsonQuestions: JSON.stringify({
        questions: [
          {
            question: 'Question 1',
            options: ['A', 'B', 'C', 'D'],
            correctOptionIndex: 1
          },
          {
            question: 'Question 2',
            options: ['A', 'B', 'C', 'D'],
            correctOptionIndex: 2
          }
        ]
      })
    };

    it('should require all answers before submitting quiz', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.submitQuiz();

      expect(component.quizSubmitError).toBe('Please answer all the questions before submitting');
      expect(mockNoteService.submitQuizResult).not.toHaveBeenCalled();
    });

    it('should show score only for anonymous users', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 2);
      component.submitQuiz();

      expect(component.showQuizResultModal).toBe(true);
      expect(component.showQuizProgressChart).toBe(false);
      expect(component.quizChartData).toEqual([]);
      expect(component.quizResultMessage).toBe('Score: 2 / 2');
      expect(mockNoteService.submitQuizResult).not.toHaveBeenCalled();
    });

    it('should show progress chart for authenticated users when history exists', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      mockNoteService.submitQuizResult.and.returnValue(of({
        quizScore: 2,
        quizMaxScore: 2,
        quizProgressPercentages: [50, 66.666]
      }));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 2);
      component.submitQuiz();

      expect(mockNoteService.submitQuizResult).toHaveBeenCalledWith(1, {
        quizScore: 2,
        quizMaxScore: 2
      });
      expect(component.showQuizResultModal).toBe(true);
      expect(component.showQuizProgressChart).toBe(true);
      expect(component.quizChartData).toEqual([
        {
          name: 'Progress',
          series: [
            { name: 'Attempt 1', value: 50 },
            { name: 'Attempt 2', value: 66.67 }
          ]
        }
      ]);
    });

    it('should hide progress chart for authenticated users when history is empty', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      mockNoteService.submitQuizResult.and.returnValue(of({
        quizScore: 1,
        quizMaxScore: 2,
        quizProgressPercentages: []
      }));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 3);
      component.submitQuiz();

      expect(component.showQuizResultModal).toBe(true);
      expect(component.showQuizProgressChart).toBe(false);
      expect(component.quizChartData).toEqual([]);
      expect(component.quizResultMessage).toBe('Score: 1 / 2');
    });

    it('should show fallback result when quiz submit request fails', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      mockNoteService.submitQuizResult.and.returnValue(throwError(() => ({ status: 500 })));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 2);
      component.submitQuiz();

      expect(component.showQuizResultModal).toBe(true);
      expect(component.showQuizProgressChart).toBe(false);
      expect(component.quizChartData).toEqual([]);
      expect(component.quizResultMessage).toBe('Score: 2 / 2');
    });

    it('should clear submit error when selecting an answer', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      component.quizSubmitError = 'Please answer all the questions before submitting';
      component.selectAnswer(0, 0);

      expect(component.quizSubmitError).toBe('');
      expect(component.selectedAnswers[0]).toBe(0);
    });

    it('should reset quiz state correctly', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      mockNoteService.submitQuizResult.and.returnValue(of({
        quizScore: 2,
        quizMaxScore: 2,
        quizProgressPercentages: [100]
      }));

      component.ngOnInit();
      fixture.detectChanges();

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 2);
      component.submitQuiz();

      component.resetQuiz();

      expect(component.quizSubmitted).toBe(false);
      expect(component.quizScore).toBe(0);
      expect(component.quizSubmitError).toBe('');
      expect(component.selectedAnswers).toEqual([-1, -1]);
    });

    it('should not submit quiz when there are no questions', () => {
      component.quizQuestions = [];
      component.submitQuiz();

      expect(mockNoteService.submitQuizResult).not.toHaveBeenCalled();
      expect(component.showQuizResultModal).toBe(false);
    });

    it('should close quiz result modal', () => {
      component.showQuizResultModal = true;
      component.closeQuizResultModal();
      expect(component.showQuizResultModal).toBe(false);
    });

    it('should evaluate correct and incorrect selected answers only after submission', () => {
      mockNoteService.getNoteById.and.returnValue(of(quizNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));
      mockNoteService.submitQuizResult.and.returnValue(of({
        quizScore: 1,
        quizMaxScore: 2,
        quizProgressPercentages: [50]
      }));

      component.ngOnInit();
      fixture.detectChanges();

      expect(component.isCorrectAnswer(0, 1)).toBe(false);
      expect(component.isIncorrectSelectedAnswer(0, 0)).toBe(false);

      component.selectAnswer(0, 1);
      component.selectAnswer(1, 0);
      component.submitQuiz();

      expect(component.isCorrectAnswer(0, 1)).toBe(true);
      expect(component.isIncorrectSelectedAnswer(1, 0)).toBe(true);
    });

    it('should return answered count from selected answers', () => {
      component.selectedAnswers = [0, -1, 2, -1];
      expect(component.getAnsweredCount()).toBe(2);
    });

    it('should derive hasQuiz and canShowQuizArea states', () => {
      component.quizQuestions = [];
      component.canEdit = false;
      expect(component.hasQuiz()).toBe(false);
      expect(component.canShowQuizArea()).toBe(false);

      component.canEdit = true;
      expect(component.canShowQuizArea()).toBe(true);

      component.canEdit = false;
      component.quizQuestions = [{ question: 'Q', options: ['A', 'B', 'C', 'D'], correctOptionIndex: 0 }];
      expect(component.hasQuiz()).toBe(true);
      expect(component.canShowQuizArea()).toBe(true);
    });

    it('should toggle quiz open section', () => {
      expect(component.isQuizOpen).toBe(false);
      component.toggleQuizSection();
      expect(component.isQuizOpen).toBe(true);
      component.toggleQuizSection();
      expect(component.isQuizOpen).toBe(false);
    });

    it('should not open generate quiz section when user cannot edit', () => {
      component.canEdit = false;
      component.quizQuestions = [];

      component.toggleQuizGenerateSection();

      expect(component.isQuizGenerateOpen).toBe(false);
    });

    it('should not open generate quiz section when quiz already exists', () => {
      component.canEdit = true;
      component.quizQuestions = [{ question: 'Q', options: ['A', 'B', 'C', 'D'], correctOptionIndex: 0 }];

      component.toggleQuizGenerateSection();

      expect(component.isQuizGenerateOpen).toBe(false);
    });

    it('should toggle generate quiz section and reset upload state on close', () => {
      component.canEdit = true;
      component.quizQuestions = [];
      component.quizUploadFile = new File(['x'], 'doc.txt', { type: 'text/plain' });
      component.quizUploadError = 'err';
      component.isQuizDragOver = true;

      component.toggleQuizGenerateSection();
      expect(component.isQuizGenerateOpen).toBe(true);

      component.toggleQuizGenerateSection();
      expect(component.isQuizGenerateOpen).toBe(false);
      expect(component.quizUploadFile).toBeNull();
      expect(component.quizUploadError).toBe('');
      expect(component.isQuizDragOver).toBe(false);
    });

    it('should handle quiz file select with no file', () => {
      const event = { target: { files: [] } } as unknown as Event;

      component.onQuizFileSelected(event);

      expect(component.quizUploadFile).toBeNull();
      expect(component.quizUploadError).toBe('');
    });

    it('should reject unsupported quiz file extension on select', () => {
      const file = new File(['x'], 'bad.exe', { type: 'application/octet-stream' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onQuizFileSelected(event);

      expect(component.quizUploadFile).toBeNull();
      expect(component.quizUploadError).toContain('Unsupported file type');
    });

    it('should accept supported quiz file extension on select', () => {
      const file = new File(['x'], 'note.pdf', { type: 'application/pdf' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onQuizFileSelected(event);

      expect(component.quizUploadFile).toEqual(file);
      expect(component.quizUploadError).toBe('');
    });

    it('should set drag-over state only when not generating', () => {
      const preventDefault = jasmine.createSpy('preventDefault');
      const dragEvent = { preventDefault } as unknown as DragEvent;

      component.quizGenerating = false;
      component.onQuizDragOver(dragEvent);
      expect(component.isQuizDragOver).toBe(true);

      component.isQuizDragOver = false;
      component.quizGenerating = true;
      component.onQuizDragOver(dragEvent);
      expect(component.isQuizDragOver).toBe(false);
    });

    it('should reset drag-over state on drag leave', () => {
      component.isQuizDragOver = true;
      component.onQuizDragLeave();
      expect(component.isQuizDragOver).toBe(false);
    });

    it('should ignore quiz drop when generating', () => {
      const preventDefault = jasmine.createSpy('preventDefault');
      const file = new File(['x'], 'note.pdf', { type: 'application/pdf' });
      const dragEvent = {
        preventDefault,
        dataTransfer: { files: [file] }
      } as unknown as DragEvent;

      component.quizGenerating = true;
      component.onQuizDrop(dragEvent);

      expect(component.quizUploadFile).toBeNull();
    });

    it('should handle quiz drop with no file', () => {
      const preventDefault = jasmine.createSpy('preventDefault');
      const dragEvent = {
        preventDefault,
        dataTransfer: { files: [] }
      } as unknown as DragEvent;

      component.quizGenerating = false;
      component.isQuizDragOver = true;
      component.quizUploadError = 'old';
      component.onQuizDrop(dragEvent);

      expect(component.isQuizDragOver).toBe(false);
      expect(component.quizUploadError).toBe('');
      expect(component.quizUploadFile).toBeNull();
    });

    it('should reject unsupported quiz drop file', () => {
      const preventDefault = jasmine.createSpy('preventDefault');
      const file = new File(['x'], 'note.zip', { type: 'application/zip' });
      const dragEvent = {
        preventDefault,
        dataTransfer: { files: [file] }
      } as unknown as DragEvent;

      component.quizGenerating = false;
      component.onQuizDrop(dragEvent);

      expect(component.quizUploadFile).toBeNull();
      expect(component.quizUploadError).toContain('Unsupported file type');
    });

    it('should accept supported quiz drop file', () => {
      const preventDefault = jasmine.createSpy('preventDefault');
      const file = new File(['x'], 'note.docx', { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });
      const dragEvent = {
        preventDefault,
        dataTransfer: { files: [file] }
      } as unknown as DragEvent;

      component.quizGenerating = false;
      component.onQuizDrop(dragEvent);

      expect(component.quizUploadFile).toEqual(file);
      expect(component.quizUploadError).toBe('');
    });

    it('should block quiz generation when user cannot edit', () => {
      spyOn(window, 'alert');
      component.canEdit = false;

      component.generateQuizQuestionsFromFile();

      expect(window.alert).toHaveBeenCalledWith('You do not have permission to generate quiz questions for this note');
      expect(mockNoteService.generateQuestionsWithAi).not.toHaveBeenCalled();
    });

    it('should require a file before quiz generation', () => {
      component.canEdit = true;
      component.quizUploadFile = null;

      component.generateQuizQuestionsFromFile();

      expect(component.quizUploadError).toBe('Please select a file first');
      expect(mockNoteService.generateQuestionsWithAi).not.toHaveBeenCalled();
    });

    it('should generate quiz successfully and open quiz section', () => {
      const generatedNote: NoteDTO = {
        ...testNote,
        jsonQuestions: JSON.stringify({
          questions: [
            { question: 'New Q', options: ['A', 'B', 'C', 'D'], correctOptionIndex: 0 }
          ]
        })
      };
      const file = new File(['x'], 'source.txt', { type: 'text/plain' });
      spyOn(window, 'alert');
      component.canEdit = true;
      component.quizUploadFile = file;
      component.isQuizGenerateOpen = true;
      mockNoteService.generateQuestionsWithAi.and.returnValue(of(generatedNote));

      component.generateQuizQuestionsFromFile();

      expect(mockNoteService.generateQuestionsWithAi).toHaveBeenCalled();
      expect(component.quizGenerating).toBe(false);
      expect(component.isQuizGenerateOpen).toBe(false);
      expect(component.isQuizOpen).toBe(true);
      expect(component.quizUploadFile).toBeNull();
      expect(component.quizQuestions.length).toBe(1);
      expect(window.alert).toHaveBeenCalledWith('Quiz questions generated successfully');
    });

    it('should route to error on quiz generation server error', () => {
      const file = new File(['x'], 'source.txt', { type: 'text/plain' });
      component.canEdit = true;
      component.quizUploadFile = file;
      mockNoteService.generateQuestionsWithAi.and.returnValue(throwError(() => ({ status: 500 })));

      component.generateQuizQuestionsFromFile();

      expect(component.quizGenerating).toBe(false);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should show backend message on quiz generation client error', () => {
      const file = new File(['x'], 'source.txt', { type: 'text/plain' });
      component.canEdit = true;
      component.quizUploadFile = file;
      mockNoteService.generateQuestionsWithAi.and.returnValue(throwError(() => ({ status: 400, error: { message: 'Invalid content' } })));

      component.generateQuizQuestionsFromFile();

      expect(component.quizGenerating).toBe(false);
      expect(component.quizUploadError).toBe('Invalid content');
    });

    it('should show generic message on quiz generation unknown client error', () => {
      const file = new File(['x'], 'source.txt', { type: 'text/plain' });
      component.canEdit = true;
      component.quizUploadFile = file;
      mockNoteService.generateQuestionsWithAi.and.returnValue(throwError(() => ({ status: 400 })));

      component.generateQuizQuestionsFromFile();

      expect(component.quizGenerating).toBe(false);
      expect(component.quizUploadError).toBe('Error generating quiz questions');
    });

    it('should parse invalid quiz payloads as empty quiz', () => {
      mockNoteService.getNoteById.and.returnValue(of({ ...testNote, jsonQuestions: 'not-json' }));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      expect(component.quizQuestions).toEqual([]);
      expect(component.selectedAnswers).toEqual([]);
    });

    it('should ignore quiz payload with invalid question schema', () => {
      const invalidSchemaNote: NoteDTO = {
        ...testNote,
        jsonQuestions: JSON.stringify({
          questions: [
            { question: '', options: ['A', 'B', 'C', 'D'], correctOptionIndex: 1 },
            { question: 'q', options: ['A', 'B'], correctOptionIndex: 1 },
            { question: 'q', options: ['A', 'B', 'C', 'D'], correctOptionIndex: 9 }
          ]
        })
      };

      mockNoteService.getNoteById.and.returnValue(of(invalidSchemaNote));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      expect(component.quizQuestions).toEqual([]);
      expect(component.selectedAnswers).toEqual([]);
    });

    it('should cap parsed quiz questions to 10 items', () => {
      const manyQuestions = Array.from({ length: 12 }).map((_, index) => ({
        question: `Question ${index}`,
        options: ['A', 'B', 'C', 'D'],
        correctOptionIndex: 0
      }));

      mockNoteService.getNoteById.and.returnValue(of({
        ...testNote,
        jsonQuestions: JSON.stringify({ questions: manyQuestions })
      }));
      mockUserService.getCurrentUser.and.returnValue(of(testUser));
      mockCommentService.getCommentsByNote.and.returnValue(of(testCommentsPage));

      component.ngOnInit();
      fixture.detectChanges();

      expect(component.quizQuestions.length).toBe(10);
      expect(component.selectedAnswers.length).toBe(10);
      expect(component.selectedAnswers.every(answer => answer === -1)).toBeTrue();
    });
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