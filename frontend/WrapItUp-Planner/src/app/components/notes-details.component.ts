import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { CommentService } from '../services/comment.service';
import { NoteDTO, NoteCategory } from '../dtos/note.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { CommentDTO } from '../dtos/comment.dto';

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
  editedCategory: NoteCategory = 'OTHERS';
  
  categories: NoteCategory[] = ['MATHS', 'SCIENCE', 'HISTORY', 'ART', 'LANGUAGES', 'OTHERS'];
  
  currentUser?: UserModelDTO;
  canEdit = false;
  canShare = false;
  
  // Share modal
  showShareModal = false;
  shareUsername = '';
  shareError = '';

  // Comments
  comments: CommentDTO[] = [];
  newCommentContent = '';
  currentPage = 0;
  pageSize = 10;
  totalComments = 0;
  hasMoreComments = false;
  loadingComments = false;
  showCommentMenu: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private noteService: NoteService,
    private userService: UserService,
    private commentService: CommentService,
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
        this.editedCategory = data.category || 'OTHERS';
        this.editedSummary = data.summary || '';
        this.editedVisibility = data.visibility || 'PRIVATE';
        
        this.loadCurrentUser();
        this.loadComments();
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
        
        else {
          this.router.navigate(['/error']);
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

    const isAdmin = this.isUserAdmin();
    const isOwner = this.note.userId === this.currentUser.id;
    
    // admins cannot share notes nor edit them
    this.canEdit = !isAdmin && isOwner;
    this.canShare = !isAdmin && isOwner; 
  }

  isUserAdmin(): boolean {
    return this.currentUser?.roles?.includes('ADMIN') ?? false;
  }

  canDeleteNote(): boolean {
    if (!this.currentUser || !this.note) {
      return false;
    }
    return this.isUserAdmin() || this.note.userId === this.currentUser.id;
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
      this.editedCategory = this.note.category || 'OTHERS';
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
      visibility: this.editedVisibility,
      category: this.editedCategory
    };

    this.noteService.updateNote(this.noteId, updatedNote).subscribe({
      next: (data) => {
        this.note = data;
        this.editMode = false;
        alert('Note updated successfully');
      },
      error: (err) => {
        console.error('Error updating note:', err);
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else if (err.error && err.error.message) {
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
    if (!this.canDeleteNote()) {
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
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else if (err.error && err.error.message) {
          alert(err.error.message);
        } else {
          alert('Error deleting note');
        }
      }
    });
  }

  loadComments(reset: boolean = true): void {
    if (reset) {
      this.currentPage = 0;
      this.comments = [];
    }

    this.loadingComments = true;
    this.commentService.getCommentsByNote(this.noteId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (reset) {
          this.comments = response.content;
        } else {
          this.comments = [...this.comments, ...response.content];
        }
        
        this.totalComments = response.totalElements;
        this.hasMoreComments = !response.last;
        this.loadingComments = false;
      },
      error: (err) => {
        console.error('Error loading comments:', err);
        this.loadingComments = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  getProfilePicUrl(comment: CommentDTO): string {
    if (!comment.userProfilePicUrl) {
      return '';
    }

    return `https://localhost${comment.userProfilePicUrl}`;
  }

  loadMoreComments(): void {
    this.currentPage++;
    this.loadComments(false);
  }

  addComment(): void {
    if (!this.newCommentContent.trim()) {
      return;
    }

    const comment: CommentDTO = {
      content: this.newCommentContent.trim()
    };

    this.commentService.createComment(this.noteId, comment).subscribe({
      next: (createdComment) => {
        this.newCommentContent = '';
        this.loadComments(true);
      },
      error: (err) => {
        console.error('Error creating comment:', err);
        if (err.status === 401) {
          alert('You must log in to comment');
        } else if (err.status === 403) {
          alert('You do not have permission to comment on this note');
        } else if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else {
          alert('Error creating comment');
        }
      }
    });
  }

  toggleCommentMenu(commentId: number): void {
    this.showCommentMenu = this.showCommentMenu === commentId ? null : commentId;
  }

  canDeleteComment(comment: CommentDTO): boolean {
    if (!this.currentUser) {
      return false;
    }
    return this.isUserAdmin() || this.currentUser.username === comment.username;
  }

  deleteComment(commentId: number): void {
    if (!confirm('Are you sure you want to delete this comment?')) {
      return;
    }

    this.commentService.deleteComment(this.noteId, commentId).subscribe({
      next: () => {
        this.showCommentMenu = null;
        this.loadComments(true);
      },
      error: (err) => {
        console.error('Error deleting comment:', err);
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else {
          alert('Error deleting comment');
        }
      }
    });
  }

  reportComment(commentId: number): void {
    if (!confirm('Are you sure you want to report this comment?')) {
      return;
    }

    this.commentService.reportComment(this.noteId, commentId).subscribe({
      next: () => {
        this.showCommentMenu = null;
        this.loadComments(true);
        alert('Comment reported successfully');
      },
      error: (err) => {
        console.error('Error reporting comment:', err);
        if (err.status === 401) {
          alert('You must log in to report a comment');
        } else if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else {
          alert('Error reporting comment');
        }
      }
    });
  }
}
