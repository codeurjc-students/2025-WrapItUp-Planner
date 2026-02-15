import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { Component, NO_ERRORS_SCHEMA } from '@angular/core';

import { MyNotesComponent } from './my-notes.component';
import { NoteService } from '../../services/note.service';
import { UserService } from '../../services/user.service';
import { UserStatus } from '../../dtos/user-status.enum';

// Dummy component for routing
@Component({ template: '' })
class DummyComponent {}

describe('MyNotesComponent', () => {
  let component: MyNotesComponent;
  let fixture: ComponentFixture<MyNotesComponent>;
  let noteService: jasmine.SpyObj<NoteService>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    const noteServiceSpy = jasmine.createSpyObj('NoteService', ['getRecentNotes', 'getSharedWithMe', 'deleteNote']);
    const userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser']);

    await TestBed.configureTestingModule({
      declarations: [ MyNotesComponent, DummyComponent ],
      imports: [ 
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([
          { path: 'login', component: DummyComponent },
          { path: '', component: DummyComponent }
        ]),
        FormsModule
      ],
      providers: [
        { provide: NoteService, useValue: noteServiceSpy },
        { provide: UserService, useValue: userServiceSpy }
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();

    noteService = TestBed.inject(NoteService) as jasmine.SpyObj<NoteService>;
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;

    // Mock default behavior
    userService.getCurrentUser.and.returnValue(of({ 
      id: 1, 
      username: 'testuser', 
      email: 'test@test.com',
      password: 'password123',
      roles: ['USER'],
      displayName: 'Test User',
      status: UserStatus.ACTIVE
    }));
    
    noteService.getRecentNotes.and.returnValue(of({ 
      content: [], 
      totalElements: 0, 
      totalPages: 0, 
      number: 0, 
      size: 10,
      first: true,
      last: true,
      empty: true
    }));

    fixture = TestBed.createComponent(MyNotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load recent notes on init', () => {
    expect(noteService.getRecentNotes).toHaveBeenCalledWith(0, 10, undefined, undefined);
  });

  it('should filter notes by category', () => {
    noteService.getRecentNotes.calls.reset();
    
    component.selectCategory('MATHS');
    
    expect(component.selectedCategory).toBe('MATHS');
    expect(component.currentPage).toBe(0);
    expect(noteService.getRecentNotes).toHaveBeenCalledWith(0, 10, 'MATHS', undefined);
  });

  it('should load shared notes when SHARED_WITH_ME is selected', () => {
    noteService.getSharedWithMe.and.returnValue(of({ 
      content: [], 
      totalElements: 0, 
      totalPages: 0, 
      number: 0, 
      size: 10,
      first: true,
      last: true,
      empty: true
    }));

    component.selectCategory('SHARED_WITH_ME');
    
    expect(component.selectedCategory).toBe('SHARED_WITH_ME');
    expect(noteService.getSharedWithMe).toHaveBeenCalledWith(0, 10, undefined);
  });

  it('should search notes by query', () => {
    noteService.getRecentNotes.calls.reset();
    component.searchQuery = 'test';
    
    component.onSearchChange();
    
    expect(component.currentPage).toBe(0);
    expect(noteService.getRecentNotes).toHaveBeenCalledWith(0, 10, undefined, 'test');
  });

  it('should load more notes on scroll', () => {
    component.currentPage = 0;
    component.hasMore = true;
    noteService.getRecentNotes.calls.reset();
    noteService.getRecentNotes.and.returnValue(of({ 
      content: [{ id: 1, title: 'Note 1', overview: 'Overview', summary: 'Summary', category: 'MATHS', visibility: 'PRIVATE', userId: 1, lastModified: '2024-01-01', jsonQuestions: '{}' }], 
      totalElements: 15, 
      totalPages: 2, 
      number: 1, 
      size: 10,
      first: false,
      last: false,
      empty: false
    }));
    
    component.loadMore();
    
    expect(component.currentPage).toBe(1);
    expect(noteService.getRecentNotes).toHaveBeenCalledWith(1, 10, undefined, undefined);
  });

  it('should delete note successfully', () => {
    const testNote = { id: 1, title: 'Test Note', overview: 'Overview', summary: 'Summary', category: 'MATHS' as const, visibility: 'PRIVATE' as const, userId: 1, lastModified: '2024-01-01', jsonQuestions: '{}' };
    component.filteredNotes = [testNote];
    noteService.deleteNote.and.returnValue(of(void 0));
    spyOn(window, 'confirm').and.returnValue(true);
    const mockEvent = new Event('click');
    
    component.deleteNote(testNote.id!, mockEvent);
    
    expect(noteService.deleteNote).toHaveBeenCalledWith(1);
    expect(component.filteredNotes.length).toBe(0);
  });

  it('should not delete note when user cancels', () => {
    const testNote = { id: 1, title: 'Test Note', overview: 'Overview', summary: 'Summary', category: 'MATHS' as const, visibility: 'PRIVATE' as const, userId: 1, lastModified: '2024-01-01', jsonQuestions: '{}' };
    component.filteredNotes = [testNote];
    spyOn(window, 'confirm').and.returnValue(false);
    const mockEvent = new Event('click');
    
    component.deleteNote(testNote.id!, mockEvent);
    
    expect(noteService.deleteNote).not.toHaveBeenCalled();
    expect(component.filteredNotes.length).toBe(1);
  });

  it('should reset to general notes when deselecting category', () => {
    component.selectedCategory = 'MATHS';
    noteService.getRecentNotes.calls.reset();
    
    component.selectCategory('MATHS');
    
    expect(component.selectedCategory).toBeNull();
    expect(noteService.getRecentNotes).toHaveBeenCalledWith(0, 10, undefined, undefined);
  });
});
