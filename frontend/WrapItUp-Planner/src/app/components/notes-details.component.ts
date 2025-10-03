import { Component } from '@angular/core';
import { AINoteService } from '../services/ainote.service';
import { AINoteDTO } from '../dtos/ainote.dto';


@Component({
  selector: 'app-ainote-detail',
  templateUrl: './notes-details.component.html'
})

export class AINoteDetailComponent {

  noteId: number = 1; 
  note?: AINoteDTO;

  constructor(private noteService: AINoteService) { }

  fetchNote(): void {
    this.noteService.getNoteById(this.noteId).subscribe({
      next: (data) => this.note = data,
      error: (err) => console.error(err)
    });
  }
}