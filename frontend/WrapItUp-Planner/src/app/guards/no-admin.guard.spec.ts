import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { NoAdminGuard } from './no-admin.guard';
import { UserService } from '../services/user.service';

describe('NoAdminGuard', () => {
  let guard: NoAdminGuard;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  beforeEach(() => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        NoAdminGuard,
        { provide: UserService, useValue: userServiceSpy }
      ]
    });

    guard = TestBed.inject(NoAdminGuard);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access for a regular user', (done) => {
    userServiceSpy.getCurrentUser.and.returnValue(of({ id: 1, username: 'user', roles: ['USER'] } as any));

    guard.canActivate({} as any, {} as any).subscribe(result => {
      expect(result).toBeTrue();
      expect(router.navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('should block admin and redirect to /', (done) => {
    userServiceSpy.getCurrentUser.and.returnValue(of({ id: 2, username: 'admin', roles: ['ADMIN'] } as any));

    guard.canActivate({} as any, {} as any).subscribe(result => {
      expect(result).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/']);
      done();
    });
  });

  it('should redirect to /login on error', (done) => {
    userServiceSpy.getCurrentUser.and.returnValue(throwError(() => new Error('Network error')));

    guard.canActivate({} as any, {} as any).subscribe(result => {
      expect(result).toBeFalse();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });
});
