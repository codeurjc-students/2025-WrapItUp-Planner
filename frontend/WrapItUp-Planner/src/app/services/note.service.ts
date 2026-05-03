import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NoteDTO } from '../dtos/note.dto';
import { Page } from '../dtos/page.dto';
import { QuizResultDTO } from '../dtos/quiz-result.dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NoteService {

  private apiUrl = `${environment.apiUrl}/api/v1/notes`;

  constructor(private http: HttpClient) { }

  getNoteById(id: number): Observable<NoteDTO> {
    return this.http.get<NoteDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  createNote(note: NoteDTO): Observable<NoteDTO> {
    return this.http.post<NoteDTO>(this.apiUrl, note, { withCredentials: true });
  }

  createNoteWithAi(formData: FormData): Observable<NoteDTO> {
    return this.http.post<NoteDTO>(`${this.apiUrl}/ai`, formData, { withCredentials: true });
  }

  generateQuestionsWithAi(id: number, formData: FormData): Observable<NoteDTO> {
    return this.http.post<NoteDTO>(`${this.apiUrl}/${id}/ai/questions`, formData, { withCredentials: true });
  }

  submitQuizResult(id: number, payload: QuizResultDTO): Observable<QuizResultDTO> {
    return this.http.post<QuizResultDTO>(`${this.apiUrl}/${id}/quiz-results`, payload, { withCredentials: true });
  }

  updateNote(id: number, note: NoteDTO): Observable<NoteDTO> {
    return this.http.put<NoteDTO>(`${this.apiUrl}/${id}`, note, { withCredentials: true });
  }

  shareNoteByUsername(id: number, username: string): Observable<NoteDTO> {
    return this.http.put<NoteDTO>(`${this.apiUrl}/${id}/share`, { username }, { withCredentials: true });
  }

  deleteNote(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  getRecentNotes(page: number = 0, size: number = 10, category?: string, search?: string): Observable<Page<NoteDTO>> {
    let params = `page=${page}&size=${size}`;
    if (category) {
      params += `&category=${category}`;
    }
    if (search) {
      params += `&search=${encodeURIComponent(search)}`;
    }
    return this.http.get<Page<NoteDTO>>(`${this.apiUrl}?${params}`, { withCredentials: true });
  }
  
  getSharedWithMe(page: number = 0, size: number = 10, search?: string): Observable<Page<NoteDTO>> {
    let params = `page=${page}&size=${size}`;
    if (search) {
      params += `&search=${encodeURIComponent(search)}`;
    }
    return this.http.get<Page<NoteDTO>>(`${this.apiUrl}/shared?${params}`, { withCredentials: true });
  }
}
