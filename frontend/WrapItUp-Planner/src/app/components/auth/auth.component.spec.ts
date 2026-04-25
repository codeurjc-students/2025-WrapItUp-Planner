import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

import { AuthComponent } from './auth.component';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { UserStatus } from '../../dtos/user-status.enum';
import { Component } from '@angular/core';


@Component({ selector: 'app-header', template: '' })
class AppHeaderStubComponent {}

describe('AuthComponent', () => {
  let component: AuthComponent;
  let fixture: ComponentFixture<AuthComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let userSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['login', 'register', 'logout']);
    userSpy = jasmine.createSpyObj('UserService', ['getCurrentUser']);

    await TestBed.configureTestingModule({
      imports: [FormsModule, RouterTestingModule.withRoutes([]), HttpClientTestingModule],
      declarations: [AuthComponent, AppHeaderStubComponent], 
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: UserService, useValue: userSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuthComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should switch to register mode based on url', () => {
    const navSpy = spyOnProperty(router, 'url', 'get').and.returnValue('/register');

    component.ngOnInit();

    expect(component.mode).toBe('register');
    expect(component.error).toBeNull();
    expect(component.message).toBeNull();
  });

  it('should default to login mode when url is not register', () => {
    spyOnProperty(router, 'url', 'get').and.returnValue('/login');

    component.ngOnInit();

    expect(component.mode).toBe('login');
  });

  it('login: should call AuthService.login and navigate to profile on success', (done) => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'SUCCESS' }));
    userSpy.getCurrentUser.and.returnValue(of({ 
      id: 1, 
      username: 'testuser', 
      email: 'test@test.com',
      password: 'password123',
      roles: ['USER'],
      displayName: 'Test User',
      status: UserStatus.ACTIVE
    }));

    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();

    setTimeout(() => {
      expect(authSpy.login).toHaveBeenCalledWith('testuser', 'password123');
      expect(userSpy.getCurrentUser).toHaveBeenCalled();
      expect(navSpy).toHaveBeenCalledWith(['/profile']);
      done();
    }, 600);
  });

  it('register: should call AuthService.register and navigate to /login on success', (done) => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'longpassword' };
    component.repeatPassword = 'longpassword';

    authSpy.register.and.returnValue(of({ message: 'Registered' }));

    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();

    setTimeout(() => {
      expect(authSpy.register).toHaveBeenCalled();
      expect(navSpy).toHaveBeenCalledWith(['/login']);
      done();
    }, 0);
  });

  it('login: should require username and password', () => {
    component.mode = 'login';
    component.username = '';
    component.password = '';

    component.onSubmit();

    expect(component.error).toBe('Username and password are required');
    expect(authSpy.login).not.toHaveBeenCalled();
  });

  it('login: should show bad credentials when status is not SUCCESS', () => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'FAIL' }));

    component.onSubmit();

    expect(component.error).toBe('Bad credentials');
  });

  it('login: should show bad credentials on non-server error', () => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(throwError(() => ({ status: 401 })));

    component.onSubmit();

    expect(component.error).toBe('Bad credentials');
  });

  it('login: should route to error on server error', () => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(throwError(() => ({ status: 500 })));
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();

    expect(navSpy).toHaveBeenCalledWith(['/error']);
  });

  it('login: should logout and redirect when user is banned', fakeAsync(() => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'SUCCESS' }));
    userSpy.getCurrentUser.and.returnValue(of({
      id: 1,
      username: 'testuser',
      email: 'test@test.com',
      password: 'password123',
      roles: ['USER'],
      displayName: 'Test User',
      status: UserStatus.BANNED
    }));
    authSpy.logout.and.returnValue(of({}));
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();
    tick(300);

    expect(authSpy.logout).toHaveBeenCalled();
    expect(navSpy).toHaveBeenCalledWith(['/banned']);
  }));

  it('login: should redirect to banned even when logout fails', fakeAsync(() => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'SUCCESS' }));
    userSpy.getCurrentUser.and.returnValue(of({
      id: 1,
      username: 'testuser',
      email: 'test@test.com',
      password: 'password123',
      roles: ['USER'],
      displayName: 'Test User',
      status: UserStatus.BANNED
    }));
    authSpy.logout.and.returnValue(throwError(() => ({ status: 500 })));
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();
    tick(300);

    expect(navSpy).toHaveBeenCalledWith(['/banned']);
  }));

  it('login: should navigate to profile when fetching user fails', fakeAsync(() => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'SUCCESS' }));
    userSpy.getCurrentUser.and.returnValue(throwError(() => ({ status: 500 })));
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();
    tick(300);

    expect(navSpy).toHaveBeenCalledWith(['/profile']);
  }));

  it('register: should require all fields', () => {
    component.mode = 'register';
    component.model = { username: '', email: '', password: '' };
    component.repeatPassword = '';

    component.onSubmit();

    expect(component.error).toBe('All fields are required');
    expect(authSpy.register).not.toHaveBeenCalled();
  });

  it('register: should require matching passwords', () => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'password123' };
    component.repeatPassword = 'different';

    component.onSubmit();

    expect(component.error).toBe('Passwords do not match');
    expect(authSpy.register).not.toHaveBeenCalled();
  });

  it('register: should require minimum password length', () => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'short' };
    component.repeatPassword = 'short';

    component.onSubmit();

    expect(component.error).toBe('Password must be at least 8 characters');
    expect(authSpy.register).not.toHaveBeenCalled();
  });

  it('register: should show server exists error when returned', () => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'longpassword' };
    component.repeatPassword = 'longpassword';

    authSpy.register.and.returnValue(throwError(() => ({
      status: 400,
      error: { message: 'User already exists' }
    })));

    component.onSubmit();

    expect(component.error).toBe('User already exists');
  });

  it('register: should navigate to error on server error', () => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'longpassword' };
    component.repeatPassword = 'longpassword';

    authSpy.register.and.returnValue(throwError(() => ({ status: 500 })));
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();

    expect(navSpy).toHaveBeenCalledWith(['/error']);
  });

  it('register: should show generic error when server message is not exists', () => {
    component.mode = 'register';
    component.model = { username: 'newuser', email: 'a@b.com', password: 'longpassword' };
    component.repeatPassword = 'longpassword';

    authSpy.register.and.returnValue(throwError(() => ({
      status: 400,
      error: { message: 'Invalid input' }
    })));

    component.onSubmit();

    expect(component.error).toBe('Registration error. Please try again.');
  });
});
