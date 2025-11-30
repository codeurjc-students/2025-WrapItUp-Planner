import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { NoteDTO } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';

@Component({
  selector: 'app-note-detail',
  templateUrl: './notes-details.component.html',
  styleUrls: ['./notes-details.component.css']
})
export class NoteDetailComponent implements OnInit {

  noteId!: number;
  note?: NoteDTO;
  editMode = false;
  editedTitle = '';
  editedOverview = '';
  editedSummary = '';
  editedVisibility: 'PUBLIC' | 'PRIVATE' = 'PRIVATE';
  
  currentUser?: UserModelDTO;
  canEdit = false;
  canShare = false;
  
  // Share modal
  showShareModal = false;
  shareUsername = '';
  shareError = '';

  constructor(
    private route: ActivatedRoute,
    private noteService: NoteService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.noteId = Number(idParam);
        this.fetchNote();
      }
    });
  }

  loadCurrentUser(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.checkPermissions();
      },
      error: (err) => {
        console.error('Error loading current user:', err);
        this.checkPermissions();
      }
    });
  }

  fetchNote(): void {
    this.noteService.getNoteById(this.noteId).subscribe({
      next: (data) => {
        this.note = data;
        this.editedTitle = data.title || '';
        this.editedOverview = data.overview || '';
        this.editedSummary = data.summary || '';
        this.editedVisibility = data.visibility || 'PRIVATE';
        
        this.loadCurrentUser();
      },
      error: (err) => {
        console.error('Error loading note:', err);
        
        if (err.status === 401) {
          alert('You must log in to view this note');
          this.router.navigate(['/login']);
        }
        
        else if (err.status === 403) {
          alert('You do not have permission to view this note');
          this.router.navigate(['/profile']);
        }
        
        else if (err.status === 404) {
          alert('Note not found');
          this.router.navigate(['/profile']);
        }
      }
    });
  }

  checkPermissions(): void {
    if (!this.note) {
      this.canEdit = false;
      this.canShare = false;
      return;
    }

    
    if (!this.currentUser) {
      this.canEdit = false;
      this.canShare = false;
      return;
    }

    
    const isOwner = this.note.userId === this.currentUser.id;
    this.canEdit = isOwner;
    this.canShare = isOwner;
  }

  openShareModal(): void {
    if (!this.canShare) {
      alert('You do not have permission to share this note');
      return;
    }
    this.showShareModal = true;
    this.shareUsername = '';
    this.shareError = '';
  }

  closeShareModal(): void {
    this.showShareModal = false;
    this.shareUsername = '';
    this.shareError = '';
  }

  shareWithUsername(): void {
    if (!this.shareUsername.trim()) {
      this.shareError = 'Please enter a username';
      return;
    }

    
    if (this.currentUser && this.shareUsername.trim() === this.currentUser.username) {
      this.shareError = 'You cannot share a note with yourself';
      return;
    }

    this.noteService.shareNoteByUsername(this.noteId, this.shareUsername.trim()).subscribe({
      next: (data) => {
        this.note = data;
        this.closeShareModal();
        alert('Note shared successfully');
      },
      error: (err) => {
        console.error('Error sharing note:', err);
        if (err.status === 404) {
          this.shareError = 'User not found';
        } else {
          this.shareError = 'Error sharing note';
        }
      }
    });
  }

  toggleEditMode(): void {
    if (!this.canEdit) {
      alert('You do not have permission to edit this note');
      return;
    }
    this.editMode = !this.editMode;
    if (!this.editMode && this.note) {
      this.editedTitle = this.note.title || '';
      this.editedOverview = this.note.overview || '';
      this.editedSummary = this.note.summary || '';
      this.editedVisibility = this.note.visibility || 'PRIVATE';
    }
  }

  saveChanges(): void {
    
    if (!this.editedTitle.trim()) {
      alert('Title cannot be empty');
      return;
    }

    const updatedNote: NoteDTO = {
      title: this.editedTitle,
      overview: this.editedOverview,
      summary: this.editedSummary,
      visibility: this.editedVisibility
    };

    this.noteService.updateNote(this.noteId, updatedNote).subscribe({
      next: (data) => {
        this.note = data;
        this.editMode = false;
        alert('Note updated successfully');
      },
      error: (err) => {
        console.error('Error updating note:', err);
        if (err.error && err.error.message) {
          alert(err.error.message);
        } else {
          alert('Error updating note');
        }
      }
    });
  }

  getSharedUsernames(): string {
    if (!this.note?.sharedWithUserIds || this.note.sharedWithUserIds.length === 0) {
      return 'Not shared';
    }
    const count = this.note.sharedWithUserIds.length;
    return `Shared with ${count} user${count > 1 ? 's' : ''}`;
  }

  deleteNote(): void {
    if (!this.canEdit) {
      alert('You do not have permission to delete this note');
      return;
    }

    if (!confirm('Are you sure you want to delete this note? This action cannot be undone.')) {
      return;
    }

    this.noteService.deleteNote(this.noteId).subscribe({
      next: () => {
        alert('Note deleted successfully');
        this.router.navigate(['/profile']);
      },
      error: (err) => {
        console.error('Error deleting note:', err);
        if (err.error && err.error.message) {
          alert(err.error.message);
        } else {
          alert('Error deleting note');
        }
      }
    });
  }
}

