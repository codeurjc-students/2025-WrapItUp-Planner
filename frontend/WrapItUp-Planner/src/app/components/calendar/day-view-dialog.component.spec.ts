import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { DayViewDialogComponent } from './day-view-dialog.component';
import { CalendarService } from '../../services/calendar.service';
import { EventColor } from '../../dtos/event-color.enum';
import { CalendarEventDTO } from '../../dtos/calendar-event.dto';
import { CalendarTaskDTO } from '../../dtos/calendar-task.dto';
import { CalendarDayDTO } from '../../dtos/calendar-day.dto';

describe('DayViewDialogComponent', () => {
  let component: DayViewDialogComponent;
  let fixture: ComponentFixture<DayViewDialogComponent>;
  let calendarServiceSpy: jasmine.SpyObj<CalendarService>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<DayViewDialogComponent>>;

  const dialogData = { year: 2026, month: 3, day: 1, date: '2026-03-01' };

  const mockEvent: CalendarEventDTO = {
    id: 1,
    userId: 1,
    title: 'Test Event',
    startDate: '2026-03-01T09:00:00',
    endDate: '2026-03-01T10:00:00',
    allDay: false,
    color: EventColor.BLUE
  };

  const mockTask: CalendarTaskDTO = {
    id: 1,
    userId: 1,
    title: 'Test Task',
    taskDate: '2026-03-01',
    completed: false
  };

  const mockDayData: CalendarDayDTO = {
    date: '2026-03-01',
    events: [mockEvent],
    tasks: [mockTask]
  };

  beforeEach(async () => {
    calendarServiceSpy = jasmine.createSpyObj('CalendarService', [
      'getDayView', 'createEvent', 'updateEvent', 'deleteEvent',
      'createTask', 'updateTask', 'toggleTaskComplete', 'deleteTask'
    ]);
    calendarServiceSpy.getDayView.and.returnValue(of(mockDayData));
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [DayViewDialogComponent],
      imports: [FormsModule, NoopAnimationsModule],
      providers: [
        { provide: CalendarService, useValue: calendarServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: dialogData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DayViewDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load day data on init', () => {
    expect(calendarServiceSpy.getDayView).toHaveBeenCalledWith(2026, 3, 1);
    expect(component.dayData).toEqual(mockDayData);
    expect(component.loading).toBeFalse();
  });

  it('should set error on day data load failure', () => {
    calendarServiceSpy.getDayView.and.returnValue(throwError(() => new Error('Error')));
    component.loadDayData();
    expect(component.error).toBe('Error loading day data');
  });

  it('openEventForm should show event form and hide task form', () => {
    component.openEventForm();
    expect(component.showEventForm).toBeTrue();
    expect(component.showTaskForm).toBeFalse();
  });

  it('openTaskForm should show task form and hide event form', () => {
    component.openTaskForm();
    expect(component.showTaskForm).toBeTrue();
    expect(component.showEventForm).toBeFalse();
  });

  it('saveEvent should set error if title is empty', () => {
    component.newEvent.title = '';
    component.saveEvent();
    expect(component.error).toBe('Event title is required');
    expect(calendarServiceSpy.createEvent).not.toHaveBeenCalled();
  });

  it('saveEvent should call createEvent for new event', () => {
    calendarServiceSpy.createEvent.and.returnValue(of(mockEvent));
    component.editingEvent = null;
    component.newEvent.title = 'New Event';
    component.newEvent.allDay = true;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';
    component.saveEvent();
    expect(calendarServiceSpy.createEvent).toHaveBeenCalled();
  });

  it('saveEvent should call updateEvent when editing', () => {
    calendarServiceSpy.updateEvent.and.returnValue(of(mockEvent));
    component.editingEvent = mockEvent;
    component.newEvent.title = 'Updated';
    component.newEvent.allDay = false;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';
    component.startTime = '09:00';
    component.endTime = '10:00';
    component.saveEvent();
    expect(calendarServiceSpy.updateEvent).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  it('saveTask should set error if title is empty', () => {
    component.newTask.title = '';
    component.saveTask();
    expect(component.error).toBe('Task title is required');
    expect(calendarServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('saveTask should call createTask for new task', () => {
    calendarServiceSpy.createTask.and.returnValue(of(mockTask));
    component.editingTask = null;
    component.newTask.title = 'New Task';
    component.saveTask();
    expect(calendarServiceSpy.createTask).toHaveBeenCalledWith(jasmine.objectContaining({ title: 'New Task' }));
  });

  it('toggleTask should call toggleTaskComplete', () => {
    calendarServiceSpy.toggleTaskComplete.and.returnValue(of({ ...mockTask, completed: true }));
    component.toggleTask(mockTask);
    expect(calendarServiceSpy.toggleTaskComplete).toHaveBeenCalledWith(1);
  });

  it('deleteEvent should call service on confirm', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    calendarServiceSpy.deleteEvent.and.returnValue(of(undefined));
    component.deleteEvent(mockEvent);
    expect(calendarServiceSpy.deleteEvent).toHaveBeenCalledWith(1);
  });

  it('deleteEvent should not call service if cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteEvent(mockEvent);
    expect(calendarServiceSpy.deleteEvent).not.toHaveBeenCalled();
  });

  it('close should close dialog with refresh', () => {
    component.close();
    expect(dialogRefSpy.close).toHaveBeenCalledWith('refresh');
  });
});
