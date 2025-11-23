import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { NoteService } from './note.service';
import { NoteDTO } from '../dtos/note.dto';

describe('NoteService', () => {
  let service: NoteService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [NoteService]
    });
    service = TestBed.inject(NoteService);
  });


  // Integration test - requires backend running on https://localhost:443
  it('should fetch note from real API', (done) => {
    const testId = 1;

    service.getNoteById(testId).subscribe({
      next: (note: NoteDTO) => {
        expect(note).toBeTruthy();
        expect(note.id).toBe(testId);
        console.log('Noted received', note);
        done();
      },
      error: (err) => {
        fail('Error while calling the real API ' + err.message);
        done();
      }
    });
  });
});
