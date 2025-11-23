import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NoteService } from '../services/note.service';
import { NoteDTO } from '../dtos/note.dto';

@Component({
  selector: 'app-note-detail',
  templateUrl: './notes-details.component.html'
})
export class NoteDetailComponent implements OnInit {

  noteId!: number;
  note?: NoteDTO;

  constructor(
    private route: ActivatedRoute,
    private noteService: NoteService
  ) {}

  ngOnInit(): void {
  this.route.paramMap.subscribe(params => {
    const idParam = params.get('id');
    if (idParam) {
      this.noteId = Number(idParam);
      console.log('ID obtenido:', this.noteId);
      this.fetchNote();
    } else {
      console.warn('No se encontró el parámetro id');
    }
  });
}
  fetchNote(): void {
    this.noteService.getNoteById(this.noteId).subscribe({
      next: (data) => this.note = data,
      error: (err) => console.error(err)
    });
  }
}
