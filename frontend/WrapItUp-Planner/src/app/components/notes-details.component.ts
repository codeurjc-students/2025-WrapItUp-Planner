import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AINoteService } from '../services/ainote.service';
import { AINoteDTO } from '../dtos/ainote.dto';

@Component({
  selector: 'app-ainote-detail',
  templateUrl: './notes-details.component.html'
})
export class AINoteDetailComponent implements OnInit {

  noteId!: number;
  note?: AINoteDTO;

  constructor(
    private route: ActivatedRoute,
    private noteService: AINoteService
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
