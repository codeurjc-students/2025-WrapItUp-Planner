import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { convertToParamMap, ActivatedRoute, Router } from '@angular/router';
import { NoteDetailComponent } from './notes-details.component';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('NoteDetailComponent', () => {
  let component: NoteDetailComponent;
  let fixture: ComponentFixture<NoteDetailComponent>;
  let mockNoteService: jasmine.SpyObj<NoteService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockNoteService = jasmine.createSpyObj('NoteService', ['getNoteById', 'updateNote', 'shareNoteByUsername']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [NoteDetailComponent],
      imports: [
        FormsModule,
        HttpClientTestingModule
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
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NoteDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch note from route param and display it', () => {
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

    mockNoteService.getNoteById.and.returnValue(of(testNote));
    mockUserService.getCurrentUser.and.returnValue(of(testUser));

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockNoteService.getNoteById).toHaveBeenCalledWith(1);
    expect(component.note).toEqual(testNote);
  });
});