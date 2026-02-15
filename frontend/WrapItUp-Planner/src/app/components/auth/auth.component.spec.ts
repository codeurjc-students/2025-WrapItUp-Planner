import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

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
});
