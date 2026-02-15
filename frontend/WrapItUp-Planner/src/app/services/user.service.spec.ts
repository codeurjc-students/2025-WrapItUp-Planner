import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from './user.service';
import { UserModelDTO } from '../dtos/user.dto';
import { UserStatus } from '../dtos/user-status.enum';
import { AuthService } from './auth.service';

describe('UserService (integration with real API)', () => {
  let service: UserService;
  let authService: AuthService;
  const TEST_USERNAME = 'genericUser';
  const TEST_PASSWORD = '12345678';
  const ADMIN_USERNAME = 'admin';
  const ADMIN_PASSWORD = '12345678';
  const TARGET_USER_ID = 1;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [UserService, AuthService]
    });
    service = TestBed.inject(UserService);
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

  it('should get user by id after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getUserById(1).subscribe({
          next: (user) => {
            expect(user).toBeDefined();
            expect(user.id).toBe(1);
            expect(user.username).toBe('genericUser');
            expect(user.email).toBeDefined();
            done();
          },
          error: (err) => {
            console.error('Error getting user by id:', err);
            fail('Failed to get user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should get current user after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: (user) => {
            expect(user).toBeDefined();
            expect(user.username).toBe('genericUser');
            expect(user.email).toBe('genericUser@example.com');
            done();
          },
          error: (err) => {
            console.error('Error getting current user:', err);
            fail('Failed to get current user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should update user after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: (currentUser) => {
            const userToUpdate: UserModelDTO = {
              ...currentUser,
              displayName: 'Updated Display Name'
            };

            service.updateUser(userToUpdate).subscribe({
              next: (updatedUser) => {
                expect(updatedUser).toBeDefined();
                expect(updatedUser.displayName).toBe('Updated Display Name');
                
                const revertUser: UserModelDTO = {
                  ...updatedUser,
                  displayName: currentUser.displayName || ''
                };
                service.updateUser(revertUser).subscribe(() => {
                  done();
                });
              },
              error: (err) => {
                console.error('Error updating user:', err);
                fail('Failed to update user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
                done();
              }
            });
          },
          error: (err) => {
            console.error('Error getting current user:', err);
            fail('Failed to get current user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should upload profile image after login', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        const mockFile = new File(['test image content'], 'test-profile.jpg', { type: 'image/jpeg' });

        service.uploadProfileImage(mockFile).subscribe({
          next: (user) => {
            expect(user).toBeDefined();
            expect(user.image).toBeDefined();
            expect(user.image).toContain('/api/v1/users/profile-image/');
            done();
          },
          error: (err) => {
            console.error('Error uploading image:', err);
            fail('Failed to upload image: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
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

  it('should ban and unban a user as admin', (done) => {
    authService.login(ADMIN_USERNAME, ADMIN_PASSWORD).subscribe({
      next: () => {
        service.banUser(TARGET_USER_ID).subscribe({
          next: (bannedUser) => {
            expect(bannedUser).toBeDefined();
            expect([UserStatus.BANNED, 'BANNED']).toContain(bannedUser.status ?? '');

            service.unbanUser(TARGET_USER_ID).subscribe({
              next: (unbannedUser) => {
                expect(unbannedUser).toBeDefined();
                expect([UserStatus.ACTIVE, 'ACTIVE']).toContain(unbannedUser.status ?? '');
                done();
              },
              error: (err) => {
                console.error('Error unbanning user:', err);
                fail('Failed to unban user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
                done();
              }
            });
          },
          error: (err) => {
            console.error('Error banning user:', err);
            fail('Failed to ban user: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
            done();
          }
        });
      },
      error: (err) => {
        console.error('Admin login error:', err);
        fail('Admin login failed: ' + (err?.error?.error || err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 20000);

  it('should handle error when getting current user without login', (done) => {
    authService.logout().subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: () => {
            fail('Should have failed with 401 error');
            done();
          },
          error: (err) => {
            expect([0, 401]).toContain(err.status);
            done();
          }
        });
      },
      error: () => {
        service.getCurrentUser().subscribe({
          next: () => {
            fail('Should have failed with 401 error');
            done();
          },
          error: (err) => {
            expect([0, 401]).toContain(err.status);
            done();
          }
        });
      }
    });
  }, 10000);

  it('should handle error when updating user with invalid data', (done) => {
    authService.login(TEST_USERNAME, TEST_PASSWORD).subscribe({
      next: () => {
        const invalidUser: UserModelDTO = {
          username: '',
          email: '',
          displayName: '',
          password: ''
        };

        service.updateUser(invalidUser).subscribe({
          next: () => {
            fail('Should have failed with 400 error');
            done();
          },
          error: (err) => {
            expect(err.status).toBeGreaterThanOrEqual(400);
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
