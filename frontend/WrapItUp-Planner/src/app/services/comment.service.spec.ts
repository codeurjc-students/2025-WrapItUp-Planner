import { TestBed } from '@angular/core/testing';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { CommentService } from './comment.service';
import { CommentDTO } from '../dtos/comment.dto';
import { AuthService } from './auth.service';

describe('CommentService (integration with real API)', () => {
  let service: CommentService;
  let authService: AuthService;
  let http: HttpClient;
  const TEST_NOTE_ID = 1;
  const TEST_USERNAME = 'genericUser';
  const TEST_PASSWORD = '12345678';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [CommentService, AuthService]
    });
    service = TestBed.inject(CommentService);
    authService = TestBed.inject(AuthService);
    http = TestBed.inject(HttpClient);
  });

  afterEach((done) => {
    authService.logout().subscribe({
      next: () => done(),
      error: () => done()
    });
  });

  afterAll((done) => {
    authService.logout().subscribe({
      next: () => done(),
      error: () => done()
    });
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch comments for a note with pagination', (done) => {
    service.getCommentsByNote(TEST_NOTE_ID, 0, 10).subscribe({
      next: (response) => {
        expect(response).toBeDefined();
        expect(response.content).toBeDefined();
        expect(Array.isArray(response.content)).toBe(true);
        expect(response.totalElements).toBeGreaterThanOrEqual(0);
        done();
      },
      error: (err) => {
        console.error('Error fetching comments:', err);
        fail('Request failed: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 10000);

  it('should create a new comment after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: (loginResponse) => {
        expect(loginResponse).toBeDefined();
        
        const newComment: CommentDTO = { 
          content: `Test comment ${Date.now()}` 
        };

        service.createComment(TEST_NOTE_ID, newComment).subscribe({
          next: (comment) => {
            expect(comment).toBeDefined();
            expect(comment.id).toBeDefined();
            expect(comment.content).toBe(newComment.content);
            expect(comment.username).toBeDefined();
            done();
          },
          error: (err) => {
            console.error('Error creating comment:', err);
            fail('Failed to create comment after login: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
            done();
          }
        });
      },
      error: (err) => {
        console.error('Login error:', err);
        fail('Login failed: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 15000);

  it('should handle errors when fetching comments for non-existent note', (done) => {
    const nonExistentNoteId = 999999;

    service.getCommentsByNote(nonExistentNoteId).subscribe({
      next: (response) => {
        expect(response).toBeDefined();
        expect(response.content).toBeDefined();
        expect(Array.isArray(response.content)).toBe(true);
        done();
      },
      error: (err) => {
        expect([401, 403]).toContain(err.status);
        done();
      }
    });
  }, 10000);
});

