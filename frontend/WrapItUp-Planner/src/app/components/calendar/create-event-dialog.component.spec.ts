import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { CreateEventDialogComponent } from './create-event-dialog.component';
import { CalendarService } from '../../services/calendar.service';
import { EventColor } from '../../dtos/event-color.enum';
import { CalendarEventDTO } from '../../dtos/calendar-event.dto';

describe('CreateEventDialogComponent', () => {
  let component: CreateEventDialogComponent;
  let fixture: ComponentFixture<CreateEventDialogComponent>;
  let calendarServiceSpy: jasmine.SpyObj<CalendarService>;
  let dialogRefSpy: jasmine.SpyObj<MatDialogRef<CreateEventDialogComponent>>;

  const mockCreatedEvent: CalendarEventDTO = {
    id: 1,
    userId: 1,
    title: 'New Event',
    startDate: '2026-03-01T09:00:00',
    endDate: '2026-03-02T10:00:00',
    allDay: false,
    color: EventColor.BLUE
  };

  beforeEach(async () => {
    calendarServiceSpy = jasmine.createSpyObj('CalendarService', ['createEvent']);
    dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

    await TestBed.configureTestingModule({
      declarations: [CreateEventDialogComponent],
      imports: [FormsModule, NoopAnimationsModule],
      providers: [
        { provide: CalendarService, useValue: calendarServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateEventDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('saveEvent should set error if title is empty', () => {
    component.newEvent.title = '';
    component.saveEvent();
    expect(component.error).toBe('Event title is required');
    expect(calendarServiceSpy.createEvent).not.toHaveBeenCalled();
  });

  it('saveEvent should set error if dates are missing', () => {
    component.newEvent.title = 'My Event';
    component.startDate = '';
    component.saveEvent();
    expect(component.error).toBe('Start and end dates are required');
  });

  it('saveEvent should use T00:00:00 / T23:59:59 for all-day events', () => {
    calendarServiceSpy.createEvent.and.returnValue(of(mockCreatedEvent));
    component.newEvent.title = 'All Day';
    component.allDay = true;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';
    component.saveEvent();
    const call = calendarServiceSpy.createEvent.calls.mostRecent().args[0];
    expect(call.startDate).toBe('2026-03-01T00:00:00');
    expect(call.endDate).toBe('2026-03-01T23:59:59');
  });

  it('saveEvent should use startTime / endTime for timed events', () => {
    calendarServiceSpy.createEvent.and.returnValue(of(mockCreatedEvent));
    component.newEvent.title = 'Timed';
    component.allDay = false;
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-01';
    component.startTime = '09:00';
    component.endTime = '10:30';
    component.saveEvent();
    const call = calendarServiceSpy.createEvent.calls.mostRecent().args[0];
    expect(call.startDate).toBe('2026-03-01T09:00:00');
    expect(call.endDate).toBe('2026-03-01T10:30:00');
  });

  it('saveEvent should set success and close dialog on creation', (done) => {
    calendarServiceSpy.createEvent.and.returnValue(of(mockCreatedEvent));
    component.newEvent.title = 'Success';
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-02';
    component.saveEvent();
    expect(component.success).toBe('Event created successfully');
    setTimeout(() => {
      expect(dialogRefSpy.close).toHaveBeenCalledWith('refresh');
      done();
    }, 1100);
  });

  it('saveEvent should set error message on failure', () => {
    calendarServiceSpy.createEvent.and.returnValue(throwError(() => ({ error: { message: 'Server error' } })));
    component.newEvent.title = 'Fail';
    component.startDate = '2026-03-01';
    component.endDate = '2026-03-02';
    component.saveEvent();
    expect(component.error).toBe('Server error');
  });

  it('cancel should close dialog', () => {
    component.cancel();
    expect(dialogRefSpy.close).toHaveBeenCalled();
  });
});
