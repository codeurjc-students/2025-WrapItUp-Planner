import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommentService } from './comment.service';

describe('CommentService (http unit)', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;

  const NOTES_URL = 'https://localhost:443/api/v1/notes';
  const ADMIN_URL = 'https://localhost:443/api/v1/admin/reported-comments';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CommentService]
    });

    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get reported comments with default pagination values', () => {
    service.getReportedComments().subscribe();

    const req = httpMock.expectOne(r => r.url === ADMIN_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10, first: true, last: true });
  });

  it('should get reported comments with provided pagination values', () => {
    service.getReportedComments(2, 25).subscribe();

    const req = httpMock.expectOne(r => r.url === ADMIN_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('25');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 2, size: 25, first: false, last: true });
  });

  it('should get comments by note with custom pagination', () => {
    service.getCommentsByNote(4, 3, 15).subscribe();

    const req = httpMock.expectOne(r => r.url === `${NOTES_URL}/4/comments`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('3');
    expect(req.request.params.get('size')).toBe('15');
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 3, size: 15, first: false, last: true });
  });
});
