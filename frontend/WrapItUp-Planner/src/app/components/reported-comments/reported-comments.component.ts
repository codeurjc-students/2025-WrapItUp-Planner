import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommentService } from '../../services/comment.service';
import { UserService } from '../../services/user.service';
import { CommentDTO } from '../../dtos/comment.dto';

@Component({
  selector: 'app-reported-comments',
  templateUrl: './reported-comments.component.html',
  styleUrls: ['./reported-comments.component.css']
})
export class ReportedCommentsComponent implements OnInit {

  reportedComments: CommentDTO[] = [];
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  hasMoreComments = false;
  loadingComments = false;

  constructor(
    private commentService: CommentService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReportedComments();
  }

  loadReportedComments(reset: boolean = true): void {
    if (reset) {
      this.currentPage = 0;
      this.reportedComments = [];
    }

    this.loadingComments = true;
    this.commentService.getReportedComments(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (reset) {
          this.reportedComments = response.content;
        } else {
          this.reportedComments = [...this.reportedComments, ...response.content];
        }
        
        this.totalElements = response.totalElements;
        this.hasMoreComments = !response.last;
        this.loadingComments = false;
      },
      error: (err) => {
        console.error('Error loading reported comments:', err);
        this.loadingComments = false;
        
        if (err.status === 401) {
          alert('You must log in to view reported comments');
          this.router.navigate(['/login']);
        } else if (err.status === 403) {
          alert('Only admins can view reported comments');
          this.router.navigate(['/']);
        } else if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  loadMoreComments(): void {
    this.currentPage++;
    this.loadReportedComments(false);
  }

  ignoreReport(comment: CommentDTO): void {
    if (!comment.id) {
      return;
    }

    this.commentService.unreportComment(comment.id).subscribe({
      next: () => {
        this.loadReportedComments(true);
      },
      error: (err) => {
        console.error('Error ignoring report:', err);
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else {
          alert('Error ignoring report');
        }
      }
    });
  }

  deleteComment(comment: CommentDTO): void {
    if (!comment.id) {
      return;
    }

    if (!confirm('Are you sure you want to delete this comment? This action cannot be undone.')) {
      return;
    }

    this.commentService.deleteReportedComment(comment.id).subscribe({
      next: () => {
        this.loadReportedComments(true);
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

  viewOriginalNote(comment: CommentDTO): void {
    if (comment.noteId) {
      this.router.navigate(['/notes', comment.noteId]);
    }
  }

  viewProfile(comment: CommentDTO): void {
    if (comment.userId) {
      this.router.navigate(['/profile'], { queryParams: { userId: comment.userId } });
    }
  }

  getProfilePicUrl(comment: CommentDTO): string {
    if (!comment.userProfilePicUrl) {
      return '';
    }
    return `https://localhost${comment.userProfilePicUrl}`;
  }
}
