import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { NoteDTO } from '../dtos/note.dto';

@Component({
  selector: 'app-create-note',
  templateUrl: './create-note.component.html',
  styleUrls: ['./create-note.component.css']
})
export class CreateNoteComponent {

  title = '';
  overview = '';
  summary = '';
  visibility: 'PUBLIC' | 'PRIVATE' = 'PRIVATE';

  constructor(
    private noteService: NoteService,
    private router: Router
  ) {}

  createNote(): void {
    
    if (!this.title.trim()) {
      alert('Title is required');
      return;
    }

    const newNote: NoteDTO = {
      title: this.title,
      overview: this.overview || '',
      summary: this.summary || '',
      visibility: this.visibility
    };

    this.noteService.createNote(newNote).subscribe({
      next: (createdNote) => {
        alert('Note created successfully');
        this.router.navigate(['/notes', createdNote.id]);
      },
      error: (err) => {
        console.error('Error creating note:', err);
        if (err.error && err.error.message) {
          alert(err.error.message);
        } else {
          alert('Error creating note');
        }
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }
}
