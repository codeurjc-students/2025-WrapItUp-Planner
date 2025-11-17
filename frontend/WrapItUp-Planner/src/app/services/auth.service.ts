import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserModelDTO } from '../dtos/user.dto';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // Direct backend URL on HTTPS port 443 for testing
  private baseUrl = 'https://localhost:443/api/v1/auth';

  constructor(private http: HttpClient) { }

  register(user: UserModelDTO): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    // include credentials in case backend expects cookies or sessions
    return this.http.post(`${this.baseUrl}/user`, user, { headers, observe: 'body', withCredentials: true });
  }

  login(username: string, password: string): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    const body = { username, password };
    // server writes tokens to cookies; we just return the response body
    return this.http.post(`${this.baseUrl}/login`, body, { headers, observe: 'body', withCredentials: true });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {}, { withCredentials: true });
  }

  refresh(refreshToken?: string): Observable<any> {
    // refresh token cookie is sent automatically if present; optionally include token explicitly
    return this.http.post(`${this.baseUrl}/refresh`, {}, { withCredentials: true });
  }
}
