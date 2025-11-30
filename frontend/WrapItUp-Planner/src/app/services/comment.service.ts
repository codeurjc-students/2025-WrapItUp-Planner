import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentDTO } from '../dtos/comment.dto';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = 'https://localhost/api/v1/notes';

  constructor(private http: HttpClient) {}

  getCommentsByNote(noteId: number, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.apiUrl}/${noteId}/comments`, { 
      params,
      withCredentials: true 
    });
  }

  createComment(noteId: number, comment: CommentDTO): Observable<CommentDTO> {
    return this.http.post<CommentDTO>(
      `${this.apiUrl}/${noteId}/comments`,
      comment,
      { withCredentials: true }
    );
  }

  deleteComment(noteId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${noteId}/comments/${commentId}`,
      { withCredentials: true }
    );
  }
}
