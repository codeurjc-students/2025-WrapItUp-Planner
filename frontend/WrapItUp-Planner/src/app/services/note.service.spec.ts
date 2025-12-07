import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NoteService } from './note.service';
import { NoteDTO } from '../dtos/note.dto';
import { Page } from '../dtos/page.dto';

describe('NoteService', () => {
  let service: NoteService;
  let httpMock: HttpTestingController;
  const apiUrl = 'https://localhost:443/api/v1/notes';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [NoteService]
    });
    service = TestBed.inject(NoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get note by id', () => {
    const testNote: NoteDTO = {
      id: 1,
      title: 'Test Note',
      overview: 'Overview',
      summary: 'Summary',
      visibility: 'PUBLIC',
      userId: 1
    };

    service.getNoteById(1).subscribe(note => {
      expect(note).toEqual(testNote);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(testNote);
  });

  it('should create a note', () => {
    const newNote: NoteDTO = {
      title: 'New Note',
      overview: 'New Overview',
      summary: 'New Summary',
      visibility: 'PRIVATE'
    };

    const createdNote: NoteDTO = {
      ...newNote,
      id: 1,
      userId: 1
    };

    service.createNote(newNote).subscribe(note => {
      expect(note).toEqual(createdNote);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newNote);
    expect(req.request.withCredentials).toBe(true);
    req.flush(createdNote);
  });

  it('should update a note', () => {
    const updatedNote: NoteDTO = {
      title: 'Updated Note',
      overview: 'Updated Overview',
      summary: 'Updated Summary',
      visibility: 'PUBLIC'
    };

    service.updateNote(1, updatedNote).subscribe(note => {
      expect(note.title).toBe('Updated Note');
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedNote);
    expect(req.request.withCredentials).toBe(true);
    req.flush(updatedNote);
  });

  it('should share note by username', () => {
    const sharedNote: NoteDTO = {
      id: 1,
      title: 'Shared Note',
      overview: 'Overview',
      summary: 'Summary',
      visibility: 'PUBLIC',
      userId: 1,
      sharedWithUserIds: [2]
    };

    service.shareNoteByUsername(1, 'otheruser').subscribe(note => {
      expect(note.sharedWithUserIds).toContain(2);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/share`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ username: 'otheruser' });
    expect(req.request.withCredentials).toBe(true);
    req.flush(sharedNote);
  });

  it('should delete a note', () => {
    service.deleteNote(1).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);
  });

  it('should handle error when getting note by id', () => {
    service.getNoteById(999).subscribe({
      next: () => fail('should have failed'),
      error: (error) => {
        expect(error.status).toBe(404);
      }
    });

    const req = httpMock.expectOne(`${apiUrl}/999`);
    req.flush('Note not found', { status: 404, statusText: 'Not Found' });
  });

  it('should handle error when creating note', () => {
    const newNote: NoteDTO = {
      title: 'New Note',
      overview: '',
      summary: '',
      visibility: 'PRIVATE'
    };

    service.createNote(newNote).subscribe({
      next: () => fail('should have failed'),
      error: (error) => {
        expect(error.status).toBe(400);
      }
    });

    const req = httpMock.expectOne(apiUrl);
    req.flush('Bad request', { status: 400, statusText: 'Bad Request' });
  });

  it('should get recent notes with pagination', () => {
    const mockPage: Page<NoteDTO> = {
      content: [
        {
          id: 1,
          title: 'Note 1',
          overview: 'Overview 1',
          summary: 'Summary 1',
          visibility: 'PUBLIC' as const,
          userId: 1
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: false
    };

    service.getRecentNotes(0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockPage);
  });

  it('should get recent notes with category filter', () => {
    const mockPage: Page<NoteDTO> = {
      content: [
        {
          id: 1,
          title: 'Math Note',
          overview: 'Overview',
          summary: 'Summary',
          visibility: 'PUBLIC' as const,
          userId: 1,
          category: 'MATHS' as const
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: false
    };

    service.getRecentNotes(0, 10, 'MATHS').subscribe(page => {
      expect(page.content[0].category).toBe('MATHS');
    });

    const req = httpMock.expectOne(`${apiUrl}?page=0&size=10&category=MATHS`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockPage);
  });

  it('should get recent notes with search filter', () => {
    const mockPage: Page<NoteDTO> = {
      content: [
        {
          id: 1,
          title: 'Test Search',
          overview: 'Overview',
          summary: 'Summary',
          visibility: 'PRIVATE' as const,
          userId: 1
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: false
    };

    service.getRecentNotes(0, 10, undefined, 'Test').subscribe(page => {
      expect(page.content[0].title).toContain('Test');
    });

    const req = httpMock.expectOne(`${apiUrl}?page=0&size=10&search=Test`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockPage);
  });

  it('should get shared with me notes', () => {
    const mockPage: Page<NoteDTO> = {
      content: [
        {
          id: 1,
          title: 'Shared Note',
          overview: 'Overview',
          summary: 'Summary',
          visibility: 'PUBLIC' as const,
          userId: 2,
          sharedWithUserIds: [1]
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: false
    };

    service.getSharedWithMe(0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.content[0].sharedWithUserIds).toContain(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/shared?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockPage);
  });

  it('should get shared with me notes with search filter', () => {
    const mockPage: Page<NoteDTO> = {
      content: [
        {
          id: 1,
          title: 'Shared Search Note',
          overview: 'Overview',
          summary: 'Summary',
          visibility: 'PUBLIC' as const,
          userId: 2,
          sharedWithUserIds: [1]
        }
      ],
      totalElements: 1,
      totalPages: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      empty: false
    };

    service.getSharedWithMe(0, 10, 'Search').subscribe(page => {
      expect(page.content[0].title).toContain('Search');
    });

    const req = httpMock.expectOne(`${apiUrl}/shared?page=0&size=10&search=Search`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockPage);
  });
});
