import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AINoteDTO } from '../dtos/ainote.dto';




@Injectable({
  providedIn: 'root'
})

export class AINoteService {

  private apiUrl = 'http://localhost:8080/api/v1/notes';

  constructor(private http: HttpClient) { }

  getNoteById(id: number) {
  return this.http.get<AINoteDTO>(`${this.apiUrl}/${id}`);
}

}