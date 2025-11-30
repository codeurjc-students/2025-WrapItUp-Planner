import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CreateNoteComponent } from './create-note.component';
import { NoteService } from '../services/note.service';
import { NoteDTO } from '../dtos/note.dto';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('CreateNoteComponent', () => {
  let component: CreateNoteComponent;
  let fixture: ComponentFixture<CreateNoteComponent>;
  let mockNoteService: jasmine.SpyObj<NoteService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockNoteService = jasmine.createSpyObj('NoteService', ['createNote']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CreateNoteComponent],
      imports: [FormsModule],
      providers: [
        { provide: NoteService, useValue: mockNoteService },
        { provide: Router, useValue: mockRouter }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateNoteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.title).toBe('');
    expect(component.overview).toBe('');
    expect(component.summary).toBe('');
    expect(component.visibility).toBe('PRIVATE');
  });

  it('should create note with valid data', () => {
    const createdNote: NoteDTO = {
      id: 1,
      title: 'Test Note',
      overview: 'Test Overview',
      summary: 'Test Summary',
      visibility: 'PRIVATE',
      userId: 1
    };

    mockNoteService.createNote.and.returnValue(of(createdNote));

    component.title = 'Test Note';
    component.overview = 'Test Overview';
    component.summary = 'Test Summary';
    component.visibility = 'PRIVATE';

    component.createNote();

    expect(mockNoteService.createNote).toHaveBeenCalledWith(jasmine.objectContaining({
      title: 'Test Note',
      overview: 'Test Overview',
      summary: 'Test Summary',
      visibility: 'PRIVATE'
    }));
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/notes', 1]);
  });

  it('should not create note with empty title', () => {
    spyOn(window, 'alert');
    component.title = '';
    component.overview = 'Test Overview';

    component.createNote();

    expect(window.alert).toHaveBeenCalledWith('Title is required');
    expect(mockNoteService.createNote).not.toHaveBeenCalled();
  });

  it('should not create note with only whitespace in title', () => {
    spyOn(window, 'alert');
    component.title = '   ';
    component.overview = 'Test Overview';

    component.createNote();

    expect(window.alert).toHaveBeenCalledWith('Title is required');
    expect(mockNoteService.createNote).not.toHaveBeenCalled();
  });

  it('should create note with PUBLIC visibility', () => {
    const createdNote: NoteDTO = {
      id: 2,
      title: 'Public Note',
      overview: '',
      summary: '',
      visibility: 'PUBLIC',
      userId: 1
    };

    mockNoteService.createNote.and.returnValue(of(createdNote));

    component.title = 'Public Note';
    component.visibility = 'PUBLIC';

    component.createNote();

    expect(mockNoteService.createNote).toHaveBeenCalledWith(jasmine.objectContaining({
      visibility: 'PUBLIC'
    }));
  });

  it('should handle error when creating note fails', () => {
    const error = { error: { message: 'Server error' } };
    mockNoteService.createNote.and.returnValue(throwError(() => error));
    spyOn(window, 'alert');
    spyOn(console, 'error');

    component.title = 'Test Note';
    component.createNote();

    expect(console.error).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Server error');
  });

  it('should navigate to profile when cancel is called', () => {
    component.cancel();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('should create note with empty optional fields', () => {
    const createdNote: NoteDTO = {
      id: 3,
      title: 'Minimal Note',
      overview: '',
      summary: '',
      visibility: 'PRIVATE',
      userId: 1
    };

    mockNoteService.createNote.and.returnValue(of(createdNote));

    component.title = 'Minimal Note';
    component.overview = '';
    component.summary = '';

    component.createNote();

    expect(mockNoteService.createNote).toHaveBeenCalledWith(jasmine.objectContaining({
      title: 'Minimal Note',
      overview: '',
      summary: ''
    }));
  });
});
