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

  it('editEvent should set times for timed events', () => {
    component.editEvent(mockEvent);
    expect(component.showEventForm).toBeTrue();
    expect(component.startTime).toBe('09:00');
    expect(component.endTime).toBe('10:00');
  });

  it('editEvent should reset times for all-day events', () => {
    const allDayEvent = { ...mockEvent, allDay: true };
    component.editEvent(allDayEvent);
    expect(component.startTime).toBe('09:00');
    expect(component.endTime).toBe('10:00');
  });

  it('editTask should set task form and hide event form', () => {
    component.editTask(mockTask);
    expect(component.showTaskForm).toBeTrue();
    expect(component.showEventForm).toBeFalse();
  });

  it('cancelForm should reset flags and messages', () => {
    component.showEventForm = true;
    component.error = 'err';
    component.success = 'ok';

    component.cancelForm();

    expect(component.showEventForm).toBeFalse();
    expect(component.showTaskForm).toBeFalse();
    expect(component.editingEvent).toBeNull();
    expect(component.editingTask).toBeNull();
    expect(component.error).toBeNull();
    expect(component.success).toBeNull();
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

  it('saveEvent should set error on update failure', () => {
    calendarServiceSpy.updateEvent.and.returnValue(throwError(() => ({ error: { message: 'Update failed' } })));
    component.editingEvent = mockEvent;
    component.newEvent.title = 'Updated';
    component.newEvent.allDay = true;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';

    component.saveEvent();

    expect(component.error).toBe('Update failed');
  });

  it('saveEvent should set error on create failure', () => {
    calendarServiceSpy.createEvent.and.returnValue(throwError(() => ({ error: { message: 'Create failed' } })));
    component.editingEvent = null;
    component.newEvent.title = 'New Event';
    component.newEvent.allDay = true;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';

    component.saveEvent();

    expect(component.error).toBe('Create failed');
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

  it('saveTask should call updateTask when editing', () => {
    calendarServiceSpy.updateTask.and.returnValue(of(mockTask));
    component.editingTask = mockTask;
    component.newTask.title = 'Updated Task';

    component.saveTask();

    expect(calendarServiceSpy.updateTask).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  it('saveTask should set error on update failure', () => {
    calendarServiceSpy.updateTask.and.returnValue(throwError(() => ({ error: { message: 'Update failed' } })));
    component.editingTask = mockTask;
    component.newTask.title = 'Updated Task';

    component.saveTask();

    expect(component.error).toBe('Update failed');
  });

  it('toggleTask should call toggleTaskComplete', () => {
    calendarServiceSpy.toggleTaskComplete.and.returnValue(of({ ...mockTask, completed: true }));
    component.toggleTask(mockTask);
    expect(calendarServiceSpy.toggleTaskComplete).toHaveBeenCalledWith(1);
  });

  it('toggleTask should set error on failure', () => {
    calendarServiceSpy.toggleTaskComplete.and.returnValue(throwError(() => ({ status: 500 })));
    component.toggleTask(mockTask);
    expect(component.error).toBe('Error updating task');
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

  it('deleteEvent should set error on failure', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    calendarServiceSpy.deleteEvent.and.returnValue(throwError(() => ({ status: 500 })));

    component.deleteEvent(mockEvent);

    expect(component.error).toBe('Error deleting event');
  });

  it('deleteTask should not call service when cancelled', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    component.deleteTask(mockTask);
    expect(calendarServiceSpy.deleteTask).not.toHaveBeenCalled();
  });

  it('deleteTask should set error on failure', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    calendarServiceSpy.deleteTask.and.returnValue(throwError(() => ({ status: 500 })));

    component.deleteTask(mockTask);

    expect(component.error).toBe('Error deleting task');
  });

  it('close should close dialog with refresh', () => {
    component.close();
    expect(dialogRefSpy.close).toHaveBeenCalledWith('refresh');
  });
});
