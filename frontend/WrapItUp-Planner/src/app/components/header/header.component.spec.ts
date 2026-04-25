import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { UserService } from '../../services/user.service';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    userServiceSpy.getCurrentUser.and.returnValue(of({
      id: 1,
      username: 'user',
      email: 'user@test.com',
      password: '',
      roles: ['USER']
    }));

    await TestBed.configureTestingModule({
      declarations: [HeaderComponent], 
      imports: [
        RouterTestingModule,
        HttpClientTestingModule,
        MatToolbarModule,               
        MatButtonModule                 
      ],
      providers: [
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should mark authenticated user and non-admin', () => {
    component.checkAuthentication();

    expect(component.isAuthenticated).toBeTrue();
    expect(component.isAdmin).toBeFalse();
  });

  it('should mark admin user', () => {
    userServiceSpy.getCurrentUser.and.returnValue(of({
      id: 2,
      username: 'admin',
      email: 'admin@test.com',
      password: '',
      roles: ['ADMIN']
    }));

    component.checkAuthentication();

    expect(component.isAuthenticated).toBeTrue();
    expect(component.isAdmin).toBeTrue();
  });

  it('should reset flags on auth error', () => {
    userServiceSpy.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));

    component.checkAuthentication();

    expect(component.isAuthenticated).toBeFalse();
    expect(component.isAdmin).toBeFalse();
  });

  it('should navigate home on goHome', () => {
    const navSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));

    component.goHome();

    expect(navSpy).toHaveBeenCalledWith(['/']);
  });
});