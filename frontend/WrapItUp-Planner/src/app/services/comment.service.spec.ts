import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommentService } from './comment.service';
import { CommentDTO } from '../dtos/comment.dto';

describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;
  const apiUrl = 'https://localhost/api/v1/notes';

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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch comments for a note with pagination', () => {
    const noteId = 1;
    const mockResponse = {
      content: [
        {
          id: 1,
          content: 'Test comment',
          noteId: 1,
          username: 'testuser',
          displayName: 'Test User',
          createdAt: '2025-01-01T10:00:00'
        }
      ],
      totalElements: 1,
      totalPages: 1,
      last: true
    };

    service.getCommentsByNote(noteId, 0, 10).subscribe(response => {
      expect(response.content.length).toBe(1);
      expect(response.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/${noteId}/comments?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockResponse);
  });

  it('should create a new comment', () => {
    const noteId = 1;
    const newComment: CommentDTO = { content: 'New test comment' };
    const createdComment: CommentDTO = {
      id: 1,
      content: 'New test comment',
      noteId: 1,
      username: 'testuser',
      displayName: 'Test User',
      createdAt: '2025-01-01T10:00:00'
    };

    service.createComment(noteId, newComment).subscribe(comment => {
      expect(comment.id).toBe(1);
      expect(comment.content).toBe('New test comment');
    });

    const req = httpMock.expectOne(`${apiUrl}/${noteId}/comments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newComment);
    expect(req.request.withCredentials).toBe(true);
    req.flush(createdComment);
  });

  it('should delete a comment', () => {
    const noteId = 1;
    const commentId = 5;

    service.deleteComment(noteId, commentId).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/${noteId}/comments/${commentId}`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);
  });

  it('should handle errors appropriately', () => {
    const noteId = 1;

    service.getCommentsByNote(noteId).subscribe(
      () => fail('should have failed'),
      (error) => {
        expect(error.status).toBe(500);
      }
    );

    const req = httpMock.expectOne(`${apiUrl}/${noteId}/comments?page=0&size=10`);
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
  });
});

