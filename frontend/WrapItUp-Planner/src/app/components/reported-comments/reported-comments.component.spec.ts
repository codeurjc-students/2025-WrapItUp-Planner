import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA, Component } from '@angular/core';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

import { ReportedCommentsComponent } from './reported-comments.component';
import { CommentService } from '../../services/comment.service';
import { UserService } from '../../services/user.service';
import { CommentDTO } from '../../dtos/comment.dto';

@Component({ template: '' })
class DummyComponent {}

describe('ReportedCommentsComponent', () => {
  let component: ReportedCommentsComponent;
  let fixture: ComponentFixture<ReportedCommentsComponent>;
  let commentServiceSpy: jasmine.SpyObj<CommentService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let router: Router;

  const samplePage = {
    content: [{ id: 1, content: 'c', noteId: 5, userId: 9 } as CommentDTO],
    totalElements: 1,
    last: true
  } as any;

  beforeEach(async () => {
    commentServiceSpy = jasmine.createSpyObj('CommentService', [
      'getReportedComments',
      'unreportComment',
      'deleteReportedComment'
    ]);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser']);

    commentServiceSpy.getReportedComments.and.returnValue(of(samplePage));

    await TestBed.configureTestingModule({
      declarations: [ReportedCommentsComponent, DummyComponent],
      imports: [
        RouterTestingModule.withRoutes([
          { path: 'login', component: DummyComponent },
          { path: '', component: DummyComponent },
          { path: 'error', component: DummyComponent },
          { path: 'notes/:id', component: DummyComponent },
          { path: 'profile', component: DummyComponent }
        ])
      ],
      providers: [
        { provide: CommentService, useValue: commentServiceSpy },
        { provide: UserService, useValue: userServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    
    fixture = TestBed.createComponent(ReportedCommentsComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load reported comments on init', () => {
    expect(commentServiceSpy.getReportedComments).toHaveBeenCalledWith(0, 10);
    expect(component.reportedComments.length).toBe(1);
    expect(component.hasMoreComments).toBeFalse();
  });

  it('should navigate to login on 401', () => {
    commentServiceSpy.getReportedComments.and.returnValue(throwError(() => ({ status: 401 })));
    const navSpy = spyOn(router, 'navigate');

    component.loadReportedComments();

    expect(navSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should navigate to home on 403', () => {
    commentServiceSpy.getReportedComments.and.returnValue(throwError(() => ({ status: 403 })));
    const navSpy = spyOn(router, 'navigate');

    component.loadReportedComments();

    expect(navSpy).toHaveBeenCalledWith(['/']);
  });

  it('should navigate to error on server error', () => {
    commentServiceSpy.getReportedComments.and.returnValue(throwError(() => ({ status: 500 })));
    const navSpy = spyOn(router, 'navigate');

    component.loadReportedComments();

    expect(navSpy).toHaveBeenCalledWith(['/error']);
  });

  it('should unreport a comment and reload list', () => {
    const reloadSpy = spyOn(component, 'loadReportedComments');
    commentServiceSpy.unreportComment.and.returnValue(of({} as any));
    const comment = { id: 7 } as CommentDTO;

    component.ignoreReport(comment);

    expect(commentServiceSpy.unreportComment).toHaveBeenCalledWith(7);
    expect(reloadSpy).toHaveBeenCalledWith(true);
  });

  it('should skip unreport when comment has no id', () => {
    component.ignoreReport({} as CommentDTO);
    expect(commentServiceSpy.unreportComment).not.toHaveBeenCalled();
  });

  it('should handle unreport error', () => {
    spyOn(window, 'alert');
    commentServiceSpy.unreportComment.and.returnValue(throwError(() => ({ status: 400 })));

    component.ignoreReport({ id: 7 } as CommentDTO);

    expect(window.alert).toHaveBeenCalledWith('Error ignoring report');
  });

  it('should delete reported comment and reload', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    const reloadSpy = spyOn(component, 'loadReportedComments');
    commentServiceSpy.deleteReportedComment.and.returnValue(of(void 0));
    const comment = { id: 8 } as CommentDTO;

    component.deleteComment(comment);

    expect(commentServiceSpy.deleteReportedComment).toHaveBeenCalledWith(8);
    expect(reloadSpy).toHaveBeenCalledWith(true);
  });

  it('should skip delete when comment has no id', () => {
    component.deleteComment({} as CommentDTO);
    expect(commentServiceSpy.deleteReportedComment).not.toHaveBeenCalled();
  });

  it('should not delete when confirmation is cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteComment({ id: 9 } as CommentDTO);
    expect(commentServiceSpy.deleteReportedComment).not.toHaveBeenCalled();
  });

  it('should handle delete error', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');
    commentServiceSpy.deleteReportedComment.and.returnValue(throwError(() => ({ status: 400 })));

    component.deleteComment({ id: 9 } as CommentDTO);

    expect(window.alert).toHaveBeenCalledWith('Error deleting comment');
  });

  it('should navigate to note when viewing original note', () => {
    const navSpy = spyOn(router, 'navigate');
    component.viewOriginalNote({ noteId: 3 } as CommentDTO);
    expect(navSpy).toHaveBeenCalledWith(['/notes', 3]);
  });

  it('should not navigate when noteId is missing', () => {
    const navSpy = spyOn(router, 'navigate');
    component.viewOriginalNote({} as CommentDTO);
    expect(navSpy).not.toHaveBeenCalled();
  });

  it('should navigate to profile when viewing profile', () => {
    const navSpy = spyOn(router, 'navigate');
    component.viewProfile({ userId: 4 } as CommentDTO);
    expect(navSpy).toHaveBeenCalledWith(['/profile'], { queryParams: { userId: 4 } });
  });

  it('should not navigate to profile when userId missing', () => {
    const navSpy = spyOn(router, 'navigate');
    component.viewProfile({} as CommentDTO);
    expect(navSpy).not.toHaveBeenCalled();
  });

  it('should return empty profile pic url when missing', () => {
    expect(component.getProfilePicUrl({} as CommentDTO)).toBe('');
  });
});
