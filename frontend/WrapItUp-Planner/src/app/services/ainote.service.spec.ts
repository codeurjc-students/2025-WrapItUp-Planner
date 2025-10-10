import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { AINoteService } from './ainote.service';
import { AINoteDTO } from '../dtos/ainote.dto';

describe('AINoteService', () => {
  let service: AINoteService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [AINoteService]
    });
    service = TestBed.inject(AINoteService);
  });


  it('should fetch note from real API', (done) => {
    const testId = 1;

    service.getNoteById(testId).subscribe({
      next: (note: AINoteDTO) => {
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
