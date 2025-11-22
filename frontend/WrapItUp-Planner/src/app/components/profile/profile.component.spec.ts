import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser', 'updateUser', 'uploadProfileImage']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['logout']);
    
    userServiceSpy.getCurrentUser.and.returnValue(of({ username: 'test', email: 'test@test.com', displayName: 'Test User', password: '' }));

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load user data on init', () => {
    expect(userServiceSpy.getCurrentUser).toHaveBeenCalled();
    expect(component.user).toEqual({ username: 'test', email: 'test@test.com', displayName: 'Test User', password: '' });
    expect(component.loading).toBe(false);
  });

  it('should handle error when loading user data', () => {
    spyOn(console, 'error'); // Suppress console.error
    userServiceSpy.getCurrentUser.and.returnValue(throwError(() => new Error('Unauthorized')));
    
    const errorComponent = TestBed.createComponent(ProfileComponent);
    const errorInstance = errorComponent.componentInstance;
    errorComponent.detectChanges();
    
    expect(errorInstance.error).toBe('Error loading user data');
    expect(errorInstance.loading).toBe(false);
  });

  it('should toggle edit mode', () => {
    component.user = { username: 'test', email: 'test@test.com', displayName: 'Test User', password: '' };
    component.isEditing = false;

    component.toggleEdit();

    expect(component.isEditing).toBe(true);
    expect(component.editedUser).toEqual(component.user);

    component.toggleEdit();

    expect(component.isEditing).toBe(false);
  });

  it('should save changes successfully', () => {
    const updatedUser = { username: 'test', email: 'newemail@test.com', displayName: 'New Name', password: '' };
    component.editedUser = updatedUser;
    component.isEditing = true;

    userServiceSpy.updateUser.and.returnValue(of(updatedUser));

    component.saveChanges();

    expect(userServiceSpy.updateUser).toHaveBeenCalledWith(updatedUser);
    expect(component.user).toEqual(updatedUser);
    expect(component.isEditing).toBe(false);
    expect(component.success).toBe('Profile updated successfully');
  });

  it('should show error when email is missing', () => {
    component.editedUser = { username: 'test', email: '', displayName: 'Test', password: '' };

    component.saveChanges();

    expect(component.error).toBe('Email is required');
    expect(userServiceSpy.updateUser).not.toHaveBeenCalled();
  });

  it('should show error when email format is invalid', () => {
    component.editedUser = { username: 'test', email: 'invalidemail', displayName: 'Test', password: '' };

    component.saveChanges();

    expect(component.error).toBe('Invalid email format');
    expect(userServiceSpy.updateUser).not.toHaveBeenCalled();
  });

  it('should handle file selection', (done) => {
    component.user = { username: 'test', email: 'test@test.com', displayName: 'Test User', password: '' };
    
    const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
    const event = {
      target: {
        files: [mockFile]
      }
    } as any;

    const updatedUser = { username: 'test', email: 'test@test.com', displayName: 'Test User', password: '', image: '/api/v1/users/profile-image/1' };
    userServiceSpy.uploadProfileImage.and.returnValue(of(updatedUser));

    component.onFileSelected(event);

    // Wait for FileReader and upload to complete
    setTimeout(() => {
      expect(userServiceSpy.uploadProfileImage).toHaveBeenCalledWith(mockFile);
      expect(component.user).toEqual(updatedUser);
      done();
    }, 100);
  });

  it('should not upload when no file is selected', () => {
    const event = {
      target: {
        files: []
      }
    } as any;

    component.onFileSelected(event);

    expect(component.selectedFile).toBeNull();
    expect(userServiceSpy.uploadProfileImage).not.toHaveBeenCalled();
  });

  it('should call logout service when logout is clicked', () => {
    authServiceSpy.logout.and.returnValue(of({}));

    component.logout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
  });

  it('should clear success message after timeout', (done) => {
    const updatedUser = { username: 'test', email: 'test@test.com', displayName: 'Test', password: '' };
    component.editedUser = updatedUser;
    userServiceSpy.updateUser.and.returnValue(of(updatedUser));

    component.saveChanges();

    expect(component.success).toBe('Profile updated successfully');

    setTimeout(() => {
      expect(component.success).toBeNull();
      done();
    }, 3100);
  });

  it('should get profile image URL correctly', () => {
    component.user = { 
      id: 5,
      username: 'test', 
      email: 'test@test.com', 
      displayName: 'Test User', 
      password: '',
      image: '/api/v1/users/profile-image/5'
    };

    const imageUrl = component.user.image;
    expect(imageUrl).toBe('/api/v1/users/profile-image/5');
  });
});
