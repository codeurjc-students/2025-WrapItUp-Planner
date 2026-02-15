import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserModelDTO } from '../dtos/user.dto';





@Injectable({
  providedIn: 'root'
})

export class UserService {

  private apiUrl = 'https://localhost:443/api/v1/users';

  constructor(private http: HttpClient) { }

  getUserById(id: number): Observable<UserModelDTO> {
    return this.http.get<UserModelDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  getCurrentUser(): Observable<UserModelDTO> {
    return this.http.get<UserModelDTO>(`${this.apiUrl}`, { withCredentials: true });
  }

  updateUser(user: UserModelDTO): Observable<UserModelDTO> {
    return this.http.put<UserModelDTO>(`${this.apiUrl}`, user, { withCredentials: true });
  }

  uploadProfileImage(file: File): Observable<UserModelDTO> {
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<UserModelDTO>(`${this.apiUrl}/upload-image`, formData, { withCredentials: true });
  }

  banUser(userId: number): Observable<UserModelDTO> {
    return this.http.post<UserModelDTO>(`${this.apiUrl}/${userId}/ban`, {}, { withCredentials: true });
  }

  unbanUser(userId: number): Observable<UserModelDTO> {
    return this.http.post<UserModelDTO>(`${this.apiUrl}/${userId}/unban`, {}, { withCredentials: true });
  }

}