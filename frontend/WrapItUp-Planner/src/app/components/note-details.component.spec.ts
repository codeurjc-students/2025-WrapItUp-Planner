import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { convertToParamMap, ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NoteDetailComponent } from './notes-details.component';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('NoteDetailComponent', () => {
  let component: NoteDetailComponent;
  let fixture: ComponentFixture<NoteDetailComponent>;
  let mockNoteService: jasmine.SpyObj<NoteService>;
  let mockUserService: jasmine.SpyObj<UserService>;
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

  beforeEach(async () => {
    mockNoteService = jasmine.createSpyObj('NoteService', ['getNoteById', 'updateNote', 'shareNoteByUsername', 'deleteNote']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
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

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockNoteService.getNoteById).toHaveBeenCalledWith(1);
    expect(component.note).toEqual(testNote);
  });

  it('should set canEdit to true when current user is the note owner', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.canEdit).toBe(true);
    expect(component.canShare).toBe(true);
  });

  it('should set canEdit to false when current user is not the note owner', () => {
    const otherUser: UserModelDTO = { ...testUser, id: 2, username: 'otheruser' };
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(otherUser));

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.canEdit).toBe(false);
    expect(component.canShare).toBe(false);
  });

  it('should toggle edit mode when toggleEditMode is called', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));

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

    component.ngOnInit();
    fixture.detectChanges();

    component.openShareModal();

    expect(component.showShareModal).toBe(true);
    expect(component.shareUsername).toBe('');
  });

  it('should share note with username', () => {
    const sharedNote: NoteDTO = { ...testNote, sharedWithUserIds: [2] };
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
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

    component.ngOnInit();
    fixture.detectChanges();

    component.shareUsername = 'testuser';
    component.shareWithUsername();

    expect(component.shareError).toBe('You cannot share a note with yourself');
    expect(mockNoteService.shareNoteByUsername).not.toHaveBeenCalled();
  });

  it('should delete note when deleteNote is called', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    mockNoteService.deleteNote.and.returnValue(of(void 0));
    spyOn(window, 'confirm').and.returnValue(true);

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(mockNoteService.deleteNote).toHaveBeenCalledWith(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('should not delete note when user cancels confirmation', () => {
    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));
    spyOn(window, 'confirm').and.returnValue(false);

    component.ngOnInit();
    fixture.detectChanges();

    component.deleteNote();

    expect(mockNoteService.deleteNote).not.toHaveBeenCalled();
  });

  it('should return shared usernames count', () => {
    mockNoteService.getNoteById.and.returnValue(of({ ...testNote, sharedWithUserIds: [2, 3] }));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));

    component.ngOnInit();
    fixture.detectChanges();

    const result = component.getSharedUsernames();
    expect(result).toBe('Shared with 2 users');
  });
});