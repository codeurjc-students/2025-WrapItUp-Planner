import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { CreateNoteComponent } from './create-note.component';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('CreateNoteComponent', () => {
  let component: CreateNoteComponent;
  let fixture: ComponentFixture<CreateNoteComponent>;
  let mockNoteService: jasmine.SpyObj<NoteService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockNoteService = jasmine.createSpyObj('NoteService', ['createNote', 'createNoteWithAi']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    
    const mockUser: UserModelDTO = {
      id: 1,
      username: 'testuser',
      email: 'test@test.com',
      displayName: 'Test User',
      password: '',
      roles: ['USER']
    };
    mockUserService.getCurrentUser.and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      declarations: [CreateNoteComponent],
      imports: [FormsModule],
      providers: [
        { provide: NoteService, useValue: mockNoteService },
        { provide: UserService, useValue: mockUserService },
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

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/my-notes']);
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

  it('should block admin users from creating notes', () => {
    mockUserService.getCurrentUser.and.returnValue(of({
      id: 2,
      username: 'admin',
      email: 'admin@test.com',
      displayName: 'Admin',
      password: '',
      roles: ['ADMIN']
    }));
    spyOn(window, 'alert');

    fixture = TestBed.createComponent(CreateNoteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(window.alert).toHaveBeenCalledWith('Administrators cannot create notes');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should reset AI state when switching back to manual mode', () => {
    component.mode = 'ai';
    component.aiFile = new File(['hi'], 'note.txt', { type: 'text/plain' });
    component.aiFileCharCount = 10;
    component.aiFileError = 'Bad file';
    component.isGenerating = true;
    component.isDragOver = true;

    component.onModeChange('manual');

    expect(component.mode).toBe('manual');
    expect(component.aiFile).toBeNull();
    expect(component.aiFileCharCount).toBe(0);
    expect(component.aiFileError).toBe('');
    expect(component.isGenerating).toBeFalse();
    expect(component.isDragOver).toBeFalse();
  });

  it('should not create AI note without file', () => {
    spyOn(window, 'alert');
    component.mode = 'ai';

    component.createNote();

    expect(window.alert).toHaveBeenCalledWith('File is required for AI notes');
    expect(mockNoteService.createNoteWithAi).not.toHaveBeenCalled();
  });

  it('should not create AI note when file validation fails', () => {
    spyOn(window, 'alert');
    component.mode = 'ai';
    component.aiFile = new File(['hi'], 'note.txt', { type: 'text/plain' });
    component.aiFileError = 'Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.';

    component.createNote();

    expect(window.alert).toHaveBeenCalledWith('Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.');
    expect(mockNoteService.createNoteWithAi).not.toHaveBeenCalled();
  });

  it('should create AI note with valid file and navigate', () => {
    const createdNote: NoteDTO = {
      id: 10,
      title: 'AI Note',
      overview: 'Overview',
      summary: 'Summary',
      visibility: 'PRIVATE',
      userId: 1
    };
    mockNoteService.createNoteWithAi.and.returnValue(of(createdNote));
    spyOn(window, 'alert');

    component.mode = 'ai';
    component.aiFile = new File(['hello'], 'note.pdf', { type: 'application/pdf' });
    component.visibility = 'PRIVATE';
    component.category = 'OTHERS';

    component.createNote();

    expect(mockNoteService.createNoteWithAi).toHaveBeenCalledWith(jasmine.any(FormData));
    expect(window.alert).toHaveBeenCalledWith('AI note created successfully');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/notes', 10]);
  });

  it('should show error message on AI note failure', () => {
    const error = { error: { message: 'AI error' }, status: 400 };
    mockNoteService.createNoteWithAi.and.returnValue(throwError(() => error));
    spyOn(window, 'alert');

    component.mode = 'ai';
    component.aiFile = new File(['hello'], 'note.pdf', { type: 'application/pdf' });

    component.createNote();

    expect(window.alert).toHaveBeenCalledWith('AI error');
  });

  it('should route to error page on AI server error', () => {
    const error = { status: 500 };
    mockNoteService.createNoteWithAi.and.returnValue(throwError(() => error));

    component.mode = 'ai';
    component.aiFile = new File(['hello'], 'note.pdf', { type: 'application/pdf' });

    component.createNote();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should toggle generating flag around AI request', () => {
    const subject = new Subject<NoteDTO>();
    mockNoteService.createNoteWithAi.and.returnValue(subject.asObservable());

    component.mode = 'ai';
    component.aiFile = new File(['hello'], 'note.pdf', { type: 'application/pdf' });

    component.createNote();
    expect(component.isGenerating).toBeTrue();

    subject.next({
      id: 99,
      title: 'AI Note',
      overview: '',
      summary: '',
      visibility: 'PRIVATE',
      userId: 1
    });
    subject.complete();

    expect(component.isGenerating).toBeFalse();
  });

  it('should flag unsupported AI file types', () => {
    const file = new File(['hello'], 'note.exe', { type: 'application/octet-stream' });
    const event = { target: { files: [file] } } as unknown as Event;

    component.onFileSelected(event);

    expect(component.aiFileError).toBe('Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.');
  });

  it('should calculate character count for text files', () => {
    const originalFileReader = window.FileReader;

    class MockFileReader {
      result: string | ArrayBuffer | null = null;
      onload: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;
      onerror: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;

      readAsText(): void {
        this.result = 'hello world';
        if (this.onload) {
          this.onload.call(this as unknown as FileReader, {} as ProgressEvent<FileReader>);
        }
      }
    }

    Object.defineProperty(window, 'FileReader', { value: MockFileReader, configurable: true });
    try {
      const file = new File(['hello world'], 'note.txt', { type: 'text/plain' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileSelected(event);

      expect(component.aiFileCharCount).toBe(11);
      expect(component.aiFileError).toBe('');
    } finally {
      Object.defineProperty(window, 'FileReader', { value: originalFileReader, configurable: true });
    }
  });

  it('should show error when text file exceeds max characters', () => {
    const originalFileReader = window.FileReader;

    class MockFileReader {
      result: string | ArrayBuffer | null = null;
      onload: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;
      onerror: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;

      readAsText(): void {
        this.result = 'x'.repeat(50001);
        if (this.onload) {
          this.onload.call(this as unknown as FileReader, {} as ProgressEvent<FileReader>);
        }
      }
    }

    Object.defineProperty(window, 'FileReader', { value: MockFileReader, configurable: true });
    try {
      const file = new File(['x'], 'note.md', { type: 'text/markdown' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileSelected(event);

      expect(component.aiFileError).toBe('File exceeds the maximum of 50000 characters');
    } finally {
      Object.defineProperty(window, 'FileReader', { value: originalFileReader, configurable: true });
    }
  });

  it('should handle file read errors for text files', () => {
    const originalFileReader = window.FileReader;

    class MockFileReader {
      result: string | ArrayBuffer | null = null;
      onload: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;
      onerror: ((this: FileReader, ev: ProgressEvent<FileReader>) => any) | null = null;

      readAsText(): void {
        if (this.onerror) {
          this.onerror.call(this as unknown as FileReader, {} as ProgressEvent<FileReader>);
        }
      }
    }

    Object.defineProperty(window, 'FileReader', { value: MockFileReader, configurable: true });
    try {
      const file = new File(['hello'], 'note.txt', { type: 'text/plain' });
      const event = { target: { files: [file] } } as unknown as Event;

      component.onFileSelected(event);

      expect(component.aiFileError).toBe('Unable to read the selected file');
    } finally {
      Object.defineProperty(window, 'FileReader', { value: originalFileReader, configurable: true });
    }
  });

  it('should manage drag over state', () => {
    const event = { preventDefault: () => undefined } as DragEvent;
    component.isGenerating = false;

    component.onDragOver(event);
    expect(component.isDragOver).toBeTrue();

    component.onDragLeave();
    expect(component.isDragOver).toBeFalse();
  });

  it('should ignore drop when generating', () => {
    const file = new File(['hello'], 'note.pdf', { type: 'application/pdf' });
    const event = {
      preventDefault: () => undefined,
      dataTransfer: { files: [file] }
    } as unknown as DragEvent;

    component.isGenerating = true;
    component.onDrop(event);

    expect(component.aiFile).toBeNull();
  });
});