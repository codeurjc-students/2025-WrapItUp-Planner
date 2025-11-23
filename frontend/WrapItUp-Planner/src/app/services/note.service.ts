import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NoteDTO } from '../dtos/note.dto';




@Injectable({
  providedIn: 'root'
})

export class NoteService {

  private apiUrl = 'https://localhost:443/api/v1/notes';

  constructor(private http: HttpClient) { }

  getNoteById(id: number): Observable<NoteDTO> {
    return this.http.get<NoteDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

}
