import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NoteService } from '../../services/note.service';
import { UserService } from '../../services/user.service';
import { NoteDTO, NoteCategory } from '../../dtos/note.dto';
import { Page } from '../../dtos/page.dto';
import { UserModelDTO } from '../../dtos/user.dto';

@Component({
  selector: 'app-my-notes',
  templateUrl: './my-notes.component.html',
  styleUrls: ['./my-notes.component.css']
})
export class MyNotesComponent implements OnInit {
  
  filteredNotes: NoteDTO[] = [];
  searchQuery: string = '';
  selectedCategory: NoteCategory | null = null;
  
  categories: NoteCategory[] = ['MATHS', 'SCIENCE', 'HISTORY', 'ART', 'LANGUAGES', 'OTHERS'];
  
  currentPage: number = 0;
  pageSize: number = 10;
  hasMore: boolean = true;

  constructor(
    private noteService: NoteService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user: UserModelDTO) => {
        if (user && user.roles && user.roles.includes('ADMIN')) {
          this.router.navigate(['/']);
          return;
        }
        this.loadRecentNotes();
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  loadRecentNotes(): void {
    this.noteService.getRecentNotes(
      this.currentPage, 
      this.pageSize, 
      this.selectedCategory || undefined,
      this.searchQuery || undefined
    ).subscribe({
      next: (response: Page<NoteDTO>) => {
        if (this.currentPage === 0) {
          this.filteredNotes = response.content || [];
        } else {
          this.filteredNotes = [...this.filteredNotes, ...(response.content || [])];
        }
        this.hasMore = !response.last;
      },
      error: (err: any) => {
        console.error('Error loading notes:', err);
      }
    });
  }

  loadMore(): void {
    if (this.hasMore) {
      this.currentPage++;
      this.loadRecentNotes();
    }
  }

  selectCategory(category: NoteCategory): void {
    if (this.selectedCategory === category) {
      this.selectedCategory = null;
    } else {
      this.selectedCategory = category;
    }
    this.currentPage = 0;
    this.filteredNotes = [];
    this.loadRecentNotes();
  }

  onSearchChange(): void {
    this.currentPage = 0;
    this.filteredNotes = [];
    this.loadRecentNotes();
  }

  createNote(): void {
    this.router.navigate(['/notes/create']);
  }

  viewNote(noteId: number | undefined): void {
    if (noteId) {
      this.router.navigate(['/notes', noteId]);
    }
  }

  deleteNote(noteId: number | undefined, event: Event): void {
    event.stopPropagation();
    
    if (!noteId) return;
    
    if (confirm('Are you sure you want to delete this note?')) {
      this.noteService.deleteNote(noteId).subscribe({
        next: () => {
          this.filteredNotes = this.filteredNotes.filter((note: NoteDTO) => note.id !== noteId);
        },
        error: (err: any) => {
          console.error('Error deleting note:', err);
          alert('Error deleting note');
        }
      });
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    
    return `${day}/${month}/${year}`;
  }

  getCategoryDisplayName(category: NoteCategory | undefined): string {
    if (!category) return 'Others';
    
    const displayNames: { [key in NoteCategory]: string } = {
      'MATHS': 'Maths',
      'SCIENCE': 'Science',
      'HISTORY': 'History',
      'ART': 'Art',
      'LANGUAGES': 'Languages',
      'OTHERS': 'Others'
    };
    
    return displayNames[category] || category;
  }

  getCategoryIcon(category: NoteCategory): string {
    const icons: { [key in NoteCategory]: string } = {
      'MATHS': '📐',
      'SCIENCE': '🔬',
      'HISTORY': '📚',
      'ART': '🎨',
      'LANGUAGES': '🌍',
      'OTHERS': '📝'
    };
    
    return icons[category] || '📝';
  }
}
