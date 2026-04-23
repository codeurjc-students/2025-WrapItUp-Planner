import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO, NoteCategory } from '../dtos/note.dto';
import { finalize } from 'rxjs';

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
  mode: 'manual' | 'ai' = 'manual';
  aiFile: File | null = null;
  aiFileCharCount = 0;
  aiFileError = '';
  isGenerating = false;
  isDragOver = false;
  readonly maxAiChars = 50000;
  readonly supportedExtensions = ['txt', 'md', 'pdf', 'docx', 'pptx'];
  
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

  onModeChange(mode: 'manual' | 'ai'): void {
    this.mode = mode;
    if (mode === 'manual') {
      this.aiFile = null;
      this.aiFileCharCount = 0;
      this.aiFileError = '';
      this.isGenerating = false;
      this.isDragOver = false;
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.processAiFile(file);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (!this.isGenerating) {
      this.isDragOver = true;
    }
  }

  onDragLeave(): void {
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    if (this.isGenerating) {
      return;
    }
    const file = event.dataTransfer?.files?.[0] ?? null;
    this.isDragOver = false;
    this.processAiFile(file);
  }

  createNote(): void {
    if (this.mode === 'ai') {
      this.createNoteWithAi();
      return;
    }

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

  private createNoteWithAi(): void {
    if (!this.aiFile) {
      alert('File is required for AI notes');
      return;
    }

    if (this.aiFileError) {
      alert(this.aiFileError);
      return;
    }

    if (this.aiFileCharCount > this.maxAiChars) {
      alert(`File exceeds the maximum of ${this.maxAiChars} characters`);
      return;
    }

    const formData = new FormData();
    formData.append('file', this.aiFile);
    formData.append('visibility', this.visibility);
    formData.append('category', this.category);

    this.isGenerating = true;
    this.noteService.createNoteWithAi(formData)
      .pipe(finalize(() => {
        this.isGenerating = false;
      }))
      .subscribe({
        next: (createdNote) => {
          alert('AI note created successfully');
          this.router.navigate(['/notes', createdNote.id]);
        },
        error: (err) => {
          console.error('Error creating AI note:', err);
          if (err.status >= 500) {
            this.router.navigate(['/error']);
          } else if (err.error && err.error.message) {
            alert(err.error.message);
          } else {
            alert('Error creating AI note');
          }
        }
      });
  }

  private processAiFile(file: File | null): void {
    this.aiFile = file;
    this.aiFileCharCount = 0;
    this.aiFileError = '';

    if (!file) {
      return;
    }

    const extension = this.getFileExtension(file.name);
    if (!extension || !this.supportedExtensions.includes(extension)) {
      this.aiFileError = 'Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.';
      return;
    }

    if (extension === 'txt' || extension === 'md') {
      const reader = new FileReader();
      reader.onload = () => {
        const content = typeof reader.result === 'string' ? reader.result : '';
        this.aiFileCharCount = content.length;
        if (this.aiFileCharCount > this.maxAiChars) {
          this.aiFileError = `File exceeds the maximum of ${this.maxAiChars} characters`;
        }
      };
      reader.onerror = () => {
        this.aiFileError = 'Unable to read the selected file';
      };
      reader.readAsText(file);
    }
  }

  private getFileExtension(filename: string): string {
    const parts = filename.toLowerCase().split('.');
    return parts.length > 1 ? parts[parts.length - 1] : '';
  }

  cancel(): void {
    this.router.navigate(['/my-notes']);
  }
}
