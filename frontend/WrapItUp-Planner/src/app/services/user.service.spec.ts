import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { UserModelDTO } from '../dtos/user.dto';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  const apiUrl = 'https://localhost:443/api/v1/users';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get user by id', () => {
    const mockUser: UserModelDTO = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      displayName: 'Test User',
      password: ''
    };

    service.getUserById(1).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockUser);
  });

  it('should get current user', () => {
    const mockUser: UserModelDTO = {
      id: 2,
      username: 'currentuser',
      email: 'current@example.com',
      displayName: 'Current User',
      password: ''
    };

    service.getCurrentUser().subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(user.username).toBe('currentuser');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockUser);
  });

  it('should update user', () => {
    const userToUpdate: UserModelDTO = {
      id: 3,
      username: 'updateuser',
      email: 'updated@example.com',
      displayName: 'Updated Name',
      password: ''
    };

    service.updateUser(userToUpdate).subscribe(user => {
      expect(user).toEqual(userToUpdate);
      expect(user.displayName).toBe('Updated Name');
      expect(user.email).toBe('updated@example.com');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('PUT');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.body).toEqual(userToUpdate);
    req.flush(userToUpdate);
  });

  it('should upload profile image', () => {
    const mockFile = new File(['image'], 'profile.jpg', { type: 'image/jpeg' });
    const mockResponse: UserModelDTO = {
      id: 4,
      username: 'imageuser',
      email: 'image@example.com',
      displayName: 'Image User',
      password: '',
      image: '/api/v1/users/profile-image/4'
    };

    service.uploadProfileImage(mockFile).subscribe(user => {
      expect(user).toEqual(mockResponse);
      expect(user.image).toBe('/api/v1/users/profile-image/4');
    });

    const req = httpMock.expectOne(`${apiUrl}/upload-image`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.body instanceof FormData).toBe(true);
    
    const formData = req.request.body as FormData;
    expect(formData.get('image')).toBe(mockFile);
    
    req.flush(mockResponse);
  });

  it('should handle error when getting current user', () => {
    const errorMessage = 'Unauthorized';

    service.getCurrentUser().subscribe(
      () => fail('should have failed with 401 error'),
      (error) => {
        expect(error.status).toBe(401);
      }
    );

    const req = httpMock.expectOne(apiUrl);
    req.flush(errorMessage, { status: 401, statusText: 'Unauthorized' });
  });

  it('should handle error when updating user', () => {
    const userToUpdate: UserModelDTO = {
      username: 'testuser',
      email: '',
      displayName: 'Test',
      password: ''
    };

    service.updateUser(userToUpdate).subscribe(
      () => fail('should have failed with 400 error'),
      (error) => {
        expect(error.status).toBe(400);
      }
    );

    const req = httpMock.expectOne(apiUrl);
    req.flush('Email is required', { status: 400, statusText: 'Bad Request' });
  });
});
