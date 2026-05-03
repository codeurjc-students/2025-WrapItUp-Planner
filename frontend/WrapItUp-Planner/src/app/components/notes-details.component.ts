import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NoteService } from '../services/note.service';
import { UserService } from '../services/user.service';
import { CommentService } from '../services/comment.service';
import { NoteDTO, NoteCategory } from '../dtos/note.dto';
import { QuizResultDTO } from '../dtos/quiz-result.dto';
import { UserModelDTO } from '../dtos/user.dto';
import { CommentDTO } from '../dtos/comment.dto';
import { finalize } from 'rxjs';
import { Observable, catchError, forkJoin, of } from 'rxjs';
import { environment } from '../../environments/environment';

interface QuizQuestion {
  question: string;
  options: string[];
  correctOptionIndex: number;
}

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

  // Quiz
  quizQuestions: QuizQuestion[] = [];
  selectedAnswers: number[] = [];
  quizSubmitted = false;
  quizScore = 0;
  quizUploadFile: File | null = null;
  quizUploadError = '';
  quizGenerating = false;
  isQuizOpen = false;
  isQuizGenerateOpen = false;
  isQuizDragOver = false;
  readonly quizSupportedExtensions = ['txt', 'md', 'pdf', 'docx', 'pptx'];
  showQuizResultModal = false;
  showQuizProgressChart = false;
  quizResultMessage = '';
  quizChartData: { name: string; series: { name: string; value: number }[] }[] = [];
  quizSubmitError = '';

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
    forkJoin({
      note: this.noteService.getNoteById(this.noteId),
      currentUser: this.getCurrentUserSafe()
    }).subscribe({
      next: (data) => {
        this.note = data.note;
        this.currentUser = data.currentUser ?? undefined;
        this.editedTitle = data.note.title || '';
        this.editedOverview = data.note.overview || '';
        this.editedSummary = data.note.summary || '';
        this.editedVisibility = data.note.visibility || 'PRIVATE';
        this.editedCategory = data.note.category || 'OTHERS';
        this.parseQuizQuestions(data.note.jsonQuestions);
        this.checkPermissions();
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
          this.router.navigate(['/my-notes']);
        }
        
        else if (err.status === 404) {
          alert('Note not found');
          this.router.navigate(['/my-notes']);
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
        this.router.navigate(['/my-notes']);
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

    return `${environment.apiUrl}${comment.userProfilePicUrl}`;
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

  onQuizFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.quizUploadError = '';

    if (!file) {
      this.quizUploadFile = null;
      return;
    }

    const extension = this.getFileExtension(file.name);
    if (!extension || !this.quizSupportedExtensions.includes(extension)) {
      this.quizUploadFile = null;
      this.quizUploadError = 'Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.';
      return;
    }

    this.quizUploadFile = file;
  }

  onQuizDragOver(event: DragEvent): void {
    event.preventDefault();
    if (!this.quizGenerating) {
      this.isQuizDragOver = true;
    }
  }

  onQuizDragLeave(): void {
    this.isQuizDragOver = false;
  }

  onQuizDrop(event: DragEvent): void {
    event.preventDefault();
    if (this.quizGenerating) {
      return;
    }

    const file = event.dataTransfer?.files?.[0] ?? null;
    this.isQuizDragOver = false;
    this.quizUploadError = '';

    if (!file) {
      this.quizUploadFile = null;
      return;
    }

    const extension = this.getFileExtension(file.name);
    if (!extension || !this.quizSupportedExtensions.includes(extension)) {
      this.quizUploadFile = null;
      this.quizUploadError = 'Unsupported file type. Use PDF, Word, PowerPoint, TXT, or MD.';
      return;
    }

    this.quizUploadFile = file;
  }

  toggleQuizSection(): void {
    this.isQuizOpen = !this.isQuizOpen;
  }

  toggleQuizGenerateSection(): void {
    if (!this.canEdit || this.hasQuiz()) {
      return;
    }
    this.isQuizGenerateOpen = !this.isQuizGenerateOpen;
    if (!this.isQuizGenerateOpen) {
      this.quizUploadFile = null;
      this.quizUploadError = '';
      this.isQuizDragOver = false;
    }
  }

  generateQuizQuestionsFromFile(): void {
    if (!this.canEdit) {
      alert('You do not have permission to generate quiz questions for this note');
      return;
    }

    if (!this.quizUploadFile) {
      this.quizUploadError = 'Please select a file first';
      return;
    }

    const formData = new FormData();
    formData.append('file', this.quizUploadFile);

    this.quizGenerating = true;
    this.noteService.generateQuestionsWithAi(this.noteId, formData)
      .pipe(finalize(() => {
        this.quizGenerating = false;
      }))
      .subscribe({
        next: (updatedNote) => {
          this.note = updatedNote;
          this.quizUploadFile = null;
          this.quizUploadError = '';
          this.isQuizGenerateOpen = false;
          this.isQuizOpen = true;
          this.parseQuizQuestions(updatedNote.jsonQuestions);
          alert('Quiz questions generated successfully');
        },
        error: (err) => {
          console.error('Error generating quiz questions:', err);
          if (err.status >= 500) {
            this.router.navigate(['/error']);
          } else if (err.error && err.error.message) {
            this.quizUploadError = err.error.message;
          } else {
            this.quizUploadError = 'Error generating quiz questions';
          }
        }
      });
  }

  selectAnswer(questionIndex: number, optionIndex: number): void {
    this.selectedAnswers[questionIndex] = optionIndex;
    if (this.quizSubmitError) {
      this.quizSubmitError = '';
    }
  }

  submitQuiz(): void {
    if (this.quizQuestions.length === 0) {
      return;
    }

    if (this.getAnsweredCount() !== this.quizQuestions.length) {
      this.quizSubmitError = 'Please answer all the questions before submitting';
      return;
    }

    this.quizSubmitError = '';

    this.quizScore = this.quizQuestions.reduce((score, question, index) => {
      return score + (this.selectedAnswers[index] === question.correctOptionIndex ? 1 : 0);
    }, 0);
    this.quizSubmitted = true;

    if (!this.currentUser) {
      this.quizResultMessage = `Score: ${this.quizScore} / ${this.quizQuestions.length}`;
      this.showQuizProgressChart = false;
      this.quizChartData = [];
      this.showQuizResultModal = true;
      return;
    }

    const payload: QuizResultDTO = {
      quizScore: this.quizScore,
      quizMaxScore: this.quizQuestions.length
    };

    this.noteService.submitQuizResult(this.noteId, payload).subscribe({
      next: (response) => {
        this.quizResultMessage = `Score: ${this.quizScore} / ${this.quizQuestions.length}`;
        const history = response.quizProgressPercentages ?? [];

        this.showQuizProgressChart = history.length > 0;
        this.quizChartData = this.showQuizProgressChart
          ? [{
              name: 'Progress',
              series: history.map((value, index) => ({
                name: `Attempt ${index + 1}`,
                value: Number(value.toFixed(2))
              }))
            }]
          : [];

        this.showQuizResultModal = true;
      },
      error: () => {
        this.quizResultMessage = `Score: ${this.quizScore} / ${this.quizQuestions.length}`;
        this.showQuizProgressChart = false;
        this.quizChartData = [];
        this.showQuizResultModal = true;
      }
    });
  }

  closeQuizResultModal(): void {
    this.showQuizResultModal = false;
  }

  resetQuiz(): void {
    this.selectedAnswers = new Array(this.quizQuestions.length).fill(-1);
    this.quizSubmitted = false;
    this.quizScore = 0;
    this.quizSubmitError = '';
  }

  getAnsweredCount(): number {
    return this.selectedAnswers.filter((answer) => answer >= 0).length;
  }

  hasQuiz(): boolean {
    return this.quizQuestions.length > 0;
  }

  canShowQuizArea(): boolean {
    return this.hasQuiz() || this.canEdit;
  }

  isCorrectAnswer(questionIndex: number, optionIndex: number): boolean {
    if (!this.quizSubmitted) {
      return false;
    }
    return this.quizQuestions[questionIndex]?.correctOptionIndex === optionIndex;
  }

  isIncorrectSelectedAnswer(questionIndex: number, optionIndex: number): boolean {
    if (!this.quizSubmitted) {
      return false;
    }

    return this.selectedAnswers[questionIndex] === optionIndex && !this.isCorrectAnswer(questionIndex, optionIndex);
  }

  private parseQuizQuestions(jsonQuestions: string | undefined): void {
    this.quizQuestions = [];
    this.selectedAnswers = [];
    this.quizSubmitted = false;
    this.quizScore = 0;
    this.quizSubmitError = '';

    if (!jsonQuestions || !jsonQuestions.trim()) {
      return;
    }

    try {
      const parsed = JSON.parse(jsonQuestions) as unknown;
      if (!parsed || typeof parsed !== 'object' || !('questions' in parsed)) {
        return;
      }

      const questions = (parsed as { questions?: unknown }).questions;
      if (!Array.isArray(questions)) {
        return;
      }

      const normalizedQuestions = questions
        .filter((question): question is QuizQuestion => this.isValidQuizQuestion(question))
        .slice(0, 10);

      if (normalizedQuestions.length === 0) {
        return;
      }

      this.quizQuestions = normalizedQuestions;
      this.selectedAnswers = new Array(this.quizQuestions.length).fill(-1);
    } catch (err) {
      console.error('Error parsing quiz questions JSON:', err);
    }
  }

  private isValidQuizQuestion(question: unknown): question is QuizQuestion {
    if (!question || typeof question !== 'object') {
      return false;
    }

    const candidate = question as { question?: unknown; options?: unknown; correctOptionIndex?: unknown };
    if (typeof candidate.question !== 'string' || !candidate.question.trim()) {
      return false;
    }

    if (!Array.isArray(candidate.options) || candidate.options.length !== 4) {
      return false;
    }

    if (candidate.options.some((option) => typeof option !== 'string' || !option.trim())) {
      return false;
    }

    if (typeof candidate.correctOptionIndex !== 'number') {
      return false;
    }

    return candidate.correctOptionIndex >= 0 && candidate.correctOptionIndex <= 3;
  }

  private getFileExtension(filename: string): string {
    const parts = filename.toLowerCase().split('.');
    return parts.length > 1 ? parts[parts.length - 1] : '';
  }

  private getCurrentUserSafe(): Observable<UserModelDTO | null> {
    try {
      const request = this.userService.getCurrentUser();
      if (!request) {
        return of(null);
      }
      return request.pipe(catchError(() => of(null)));
    } catch {
      return of(null);
    }
  }
}
