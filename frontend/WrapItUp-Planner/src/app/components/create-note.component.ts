import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO, NoteCategory } from '../dtos/note.dto';

@Component({
  selector: 'app-create-note',
  templateUrl: './create-note.component.html',
  styleUrls: ['./create-note.component.css']
})
export class CreateNoteComponent implements OnInit {

  title = '';
  overview = '';
  summary = '';
  visibility: 'PUBLIC' | 'PRIVATE' = 'PRIVATE';
  category: NoteCategory = 'OTHERS';
  
  categories: NoteCategory[] = ['MATHS', 'SCIENCE', 'HISTORY', 'ART', 'LANGUAGES', 'OTHERS'];

  constructor(
    private noteService: NoteService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        if (user?.roles?.includes('ADMIN')) {
          alert('Administrators cannot create notes');
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        console.error('Error loading user:', err);
      }
    });
  }

  createNote(): void {
    
    if (!this.title.trim()) {
      alert('Title is required');
      return;
    }

    const newNote: NoteDTO = {
      title: this.title,
      overview: this.overview || '',
      summary: this.summary || '',
      visibility: this.visibility,
      category: this.category
    };

    this.noteService.createNote(newNote).subscribe({
      next: (createdNote) => {
        alert('Note created successfully');
        this.router.navigate(['/notes', createdNote.id]);
      },
      error: (err) => {
        console.error('Error creating note:', err);
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else if (err.error && err.error.message) {
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
