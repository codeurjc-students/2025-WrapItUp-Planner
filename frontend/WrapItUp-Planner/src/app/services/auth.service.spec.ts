import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';

import { AuthService } from './auth.service';
import { UserModelDTO } from '../dtos/user.dto';

describe('AuthService (integration with real API)', () => {
  let service: AuthService;
  // create a unique suffix for this test run to avoid duplicate-user errors
  const _ts = Date.now();
  const TEST_USER = `u_test_integration_${_ts}`;
  const TEST_EMAIL = `atest+${_ts}@example.com`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('register should call real API (POST /user)', (done) => {
    const payload: UserModelDTO = { username: TEST_USER, email: TEST_EMAIL, password: 'pwd12345' };

    // We call the real API. The test asserts that the Observable emits or errors.
    service.register(payload).subscribe({
      next: (res) => {
        // success path depends on backend; just ensure we received a response object
        expect(res).toBeDefined();
        done();
      },
      error: (err) => {
        fail('Register request failed: ' + (err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 20000);

  it('login should call real API (POST /login)', (done) => {
    service.login(TEST_USER, 'pwd12345').subscribe({
      next: (res) => {
        expect(res).toBeDefined();
        done();
      },
      error: (err) => {
        fail('Login request failed: ' + (err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 10000);

  it('logout should call real API (POST /logout)', (done) => {
    service.logout().subscribe({
      next: (res) => {
        expect(res).toBeDefined();
        done();
      },
      error: (err) => {
        fail('Logout request failed: ' + (err?.message || JSON.stringify(err)));
        done();
      }
    });
  }, 10000);
});
