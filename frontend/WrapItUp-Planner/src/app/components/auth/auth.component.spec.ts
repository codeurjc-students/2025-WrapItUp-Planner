import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthComponent } from './auth.component';
import { AuthService } from '../../services/auth.service';
import { Component } from '@angular/core';


@Component({ selector: 'app-header', template: '' })
class AppHeaderStubComponent {}

describe('AuthComponent', () => {
  let component: AuthComponent;
  let fixture: ComponentFixture<AuthComponent>;
  let authSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthService', ['login', 'register']);

    await TestBed.configureTestingModule({
      imports: [FormsModule, RouterTestingModule.withRoutes([])],
      declarations: [AuthComponent, AppHeaderStubComponent], 
      providers: [
        { provide: AuthService, useValue: authSpy }
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

  it('login: should call AuthService.login and navigate to root on success', (done) => {
    component.mode = 'login';
    component.username = 'testuser';
    component.password = 'password123';

    authSpy.login.and.returnValue(of({ status: 'SUCCESS' }));

    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.onSubmit();

    setTimeout(() => {
      expect(authSpy.login).toHaveBeenCalledWith('testuser', 'password123');
      expect(navSpy).toHaveBeenCalledWith(['/']);
      done();
    }, 0);
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
