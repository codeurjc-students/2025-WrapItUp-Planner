import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { NoteService } from './note.service';
import { AuthService } from './auth.service';
import { NoteDTO } from '../dtos/note.dto';

describe('NoteService (integration with real API)', () => {
  let service: NoteService;
  let authService: AuthService;
  const TEST_USERNAME = 'genericUser';
  const TEST_PASSWORD = '12345678';
  let createdNoteId: number;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [NoteService, AuthService]
    });
    service = TestBed.inject(NoteService);
    authService = TestBed.inject(AuthService);
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

  it('should get note by id', (done) => {
    service.getNoteById(1).subscribe({
      next: (note) => {
        expect(note).toBeDefined();
        expect(note.id).toBe(1);
        expect(note.title).toBeDefined();
        done();
      },
      error: (err) => {
        console.error('Error getting note:', err);
        fail('Request failed: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 10000);

  it('should create a note after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        const newNote: NoteDTO = {
          title: `Test Note ${Date.now()}`,
          overview: 'Test Overview',
          summary: 'Test Summary',
          visibility: 'PRIVATE',
          category: 'OTHERS'
        };

        service.createNote(newNote).subscribe({
          next: (note) => {
            expect(note).toBeDefined();
            expect(note.id).toBeDefined();
            expect(note.title).toBe(newNote.title);
            createdNoteId = note.id!;
            done();
          },
          error: (err) => {
            console.error('Error creating note:', err);
            fail('Failed to create note: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should update a note after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        const newNote: NoteDTO = {
          title: `Note to Update ${Date.now()}`,
          overview: 'Original Overview',
          summary: 'Original Summary',
          visibility: 'PRIVATE',
          category: 'OTHERS'
        };

        service.createNote(newNote).subscribe({
          next: (createdNote) => {
            const updatedNote: NoteDTO = {
              title: 'Updated Title',
              overview: 'Updated Overview',
              summary: 'Updated Summary',
              visibility: 'PUBLIC',
              category: 'MATHS'
            };

            service.updateNote(createdNote.id!, updatedNote).subscribe({
              next: (note) => {
                expect(note.title).toBe('Updated Title');
                expect(note.overview).toBe('Updated Overview');
                done();
              },
              error: (err) => {
                console.error('Error updating note:', err);
                fail('Failed to update note: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
                done();
              }
            });
          },
          error: (err) => {
            console.error('Error creating note for update:', err);
            fail('Failed to create note for update: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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
  }, 20000);

  it('should delete a note after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        const newNote: NoteDTO = {
          title: `Note to Delete ${Date.now()}`,
          overview: 'Overview',
          summary: 'Summary',
          visibility: 'PRIVATE',
          category: 'OTHERS'
        };

        service.createNote(newNote).subscribe({
          next: (createdNote) => {
            service.deleteNote(createdNote.id!).subscribe({
              next: () => {
                expect(true).toBe(true);
                done();
              },
              error: (err) => {
                console.error('Error deleting note:', err);
                fail('Failed to delete note: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
                done();
              }
            });
          },
          error: (err) => {
            console.error('Error creating note for delete:', err);
            fail('Failed to create note for delete: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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
  }, 20000);

  it('should get recent notes with pagination', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getRecentNotes(0, 10).subscribe({
          next: (page) => {
            expect(page).toBeDefined();
            expect(page.content).toBeDefined();
            expect(Array.isArray(page.content)).toBe(true);
            expect(page.totalElements).toBeGreaterThanOrEqual(0);
            done();
          },
          error: (err) => {
            console.error('Error getting recent notes:', err);
            fail('Failed to get recent notes: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should get recent notes with category filter', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getRecentNotes(0, 10, 'SCIENCE').subscribe({
          next: (page) => {
            expect(page).toBeDefined();
            expect(page.content).toBeDefined();
            if (page.content.length > 0) {
              page.content.forEach(note => {
                expect(note.category).toBe('SCIENCE');
              });
            }
            done();
          },
          error: (err) => {
            console.error('Error getting notes by category:', err);
            fail('Failed to get notes by category: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should get shared with me notes', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getSharedWithMe(0, 10).subscribe({
          next: (page) => {
            expect(page).toBeDefined();
            expect(page.content).toBeDefined();
            expect(Array.isArray(page.content)).toBe(true);
            done();
          },
          error: (err) => {
            console.error('Error getting shared notes:', err);
            fail('Failed to get shared notes: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should get recent notes with search filter', (done) => {
    authService.login('genericUser', '12345678').subscribe({
      next: () => {
        service.getRecentNotes(0, 10, 'OTHERS').subscribe({
          next: (page) => {
            expect(page).toBeDefined();
            expect(page.content).toBeDefined();
            expect(Array.isArray(page.content)).toBe(true);
            done();
          },
          error: (err) => {
            console.error('Error getting recent notes with filter:', err);
            fail('Failed to get recent notes: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should get shared with me notes with search filter', (done) => {
    authService.login('genericUser', '12345678').subscribe({
      next: () => {
        service.getSharedWithMe(0, 10, 'Pythagorean').subscribe({
          next: (page) => {
            expect(page).toBeDefined();
            expect(page.content).toBeDefined();
            expect(Array.isArray(page.content)).toBe(true);
            done();
          },
          error: (err) => {
            console.error('Error getting shared notes with filter:', err);
            fail('Failed to get shared notes: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should share note by username', (done) => {
    authService.login('genericUser', '12345678').subscribe({
      next: () => {
        const newNote: NoteDTO = {
          title: 'Note to Share',
          summary: 'Test sharing',
          visibility: 'PRIVATE',
          category: 'OTHERS'
        };

        service.createNote(newNote).subscribe({
          next: (createdNote) => {
            service.shareNoteByUsername(createdNote.id!, 'secondUser').subscribe({
              next: () => {
                expect(true).toBe(true);
                service.deleteNote(createdNote.id!).subscribe(() => {
                  done();
                });
              },
              error: (err) => {
                console.error('Error sharing note:', err);
                service.deleteNote(createdNote.id!).subscribe(() => {
                  fail('Share failed: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
                  done();
                });
              }
            });
          },
          error: (err) => {
            console.error('Error creating note for sharing:', err);
            fail('Failed to create note: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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
  }, 20000);

  it('should handle errors when getting note by invalid id', (done) => {
    authService.login('genericUser', '12345678').subscribe({
      next: () => {
        service.getNoteById(999999).subscribe({
          next: () => {
            fail('Should have returned an error');
            done();
          },
          error: (err) => {
            expect(err.status).toBe(404);
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

  it('should handle errors when deleting non-existent note', (done) => {
    authService.login('genericUser', '12345678').subscribe({
      next: () => {
        service.deleteNote(999999).subscribe({
          next: () => {
            fail('Should have returned an error');
            done();
          },
          error: (err) => {
            expect(err.status).toBe(403);
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
});
