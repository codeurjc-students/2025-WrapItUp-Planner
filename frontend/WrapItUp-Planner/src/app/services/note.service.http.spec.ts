import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NoteService } from './note.service';
import { NoteDTO } from '../dtos/note.dto';
import { environment } from '../../environments/environment';

describe('NoteService (http unit)', () => {
  let service: NoteService;
  let httpMock: HttpTestingController;
  const BASE_URL = `${environment.apiUrl}/api/v1/notes`;

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

  it('should request recent notes without optional filters', () => {
    service.getRecentNotes().subscribe();

    const req = httpMock.expectOne(`${BASE_URL}?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('should request recent notes with category only', () => {
    service.getRecentNotes(1, 20, 'SCIENCE').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}?page=1&size=20&category=SCIENCE`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 1, size: 20, first: true, last: true });
  });

  it('should request recent notes with search only (encoded)', () => {
    service.getRecentNotes(0, 10, undefined, 'linear algebra').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}?page=0&size=10&search=linear%20algebra`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('should request recent notes with category and search', () => {
    service.getRecentNotes(2, 5, 'MATHS', 'eigen values').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}?page=2&size=5&category=MATHS&search=eigen%20values`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 2, size: 5, first: false, last: true });
  });

  it('should request shared notes without search', () => {
    service.getSharedWithMe().subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/shared?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('should request shared notes with search', () => {
    service.getSharedWithMe(3, 25, 'physics basics').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/shared?page=3&size=25&search=physics%20basics`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 3, size: 25, first: false, last: true });
  });

  it('should post quiz result with credentials', () => {
    service.submitQuizResult(4, { quizScore: 3, quizMaxScore: 5 }).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/4/quiz-results`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.body).toEqual({ quizScore: 3, quizMaxScore: 5 });
    req.flush({ quizScore: 3, quizMaxScore: 5, quizProgressPercentages: [60] });
  });

  it('should put share payload with username field', () => {
    service.shareNoteByUsername(9, 'alice').subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/9/share`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ username: 'alice' });
    req.flush({ id: 9 } as NoteDTO);
  });
});
