import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { CalendarComponent } from './calendar.component';
import { CalendarService } from '../../services/calendar.service';
import { CalendarMonthDTO } from '../../dtos/calendar-month.dto';

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let calendarServiceSpy: jasmine.SpyObj<CalendarService>;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  const mockMonthData: CalendarMonthDTO = {
    year: 2026,
    month: 3,
    days: [
      { date: '2026-03-01', eventColors: ['#3B82F6'], totalEvents: 1, pendingTasks: 0 },
      { date: '2026-03-15', eventColors: [], totalEvents: 0, pendingTasks: 2 }
    ]
  };

  beforeEach(async () => {
    calendarServiceSpy = jasmine.createSpyObj('CalendarService', ['getMonthView']);
    calendarServiceSpy.getMonthView.and.returnValue(of(mockMonthData));

    const mockDialogRef = jasmine.createSpyObj<MatDialogRef<any>>(['afterClosed', 'close']);
    mockDialogRef.afterClosed.and.returnValue(of('refresh'));
    dialogSpy = jasmine.createSpyObj('MatDialog', { open: mockDialogRef });

    await TestBed.configureTestingModule({
      declarations: [CalendarComponent],
      imports: [NoopAnimationsModule],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: CalendarService, useValue: calendarServiceSpy },
        { provide: MatDialog, useValue: dialogSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load month data on init', () => {
    expect(calendarServiceSpy.getMonthView).toHaveBeenCalledWith(component.currentYear, component.currentMonth);
    expect(component.monthData).toEqual(mockMonthData);
    expect(component.loading).toBeFalse();
  });

  it('should build calendar grid with 7-slot rows', () => {
    expect(component.calendarDays.length).toBeGreaterThan(0);
    component.calendarDays.forEach(week => expect(week.length).toBe(7));
  });

  it('should set error message on load failure', () => {
    calendarServiceSpy.getMonthView.and.returnValue(throwError(() => new Error('Network error')));
    component.loadMonth();
    expect(component.error).toBe('Error loading calendar data');
    expect(component.loading).toBeFalse();
  });

  it('previousMonth should wrap to December when going back from January', () => {
    component.currentMonth = 1;
    component.currentYear = 2026;
    component.previousMonth();
    expect(component.currentMonth).toBe(12);
    expect(component.currentYear).toBe(2025);
  });

  it('nextMonth should wrap to January when going forward from December', () => {
    component.currentMonth = 12;
    component.currentYear = 2025;
    component.nextMonth();
    expect(component.currentMonth).toBe(1);
    expect(component.currentYear).toBe(2026);
  });

  it('openDayView should not open dialog for null day', () => {
    component.openDayView(null);
    expect(dialogSpy.open).not.toHaveBeenCalled();
  });

  it('openDayView should open dialog and reload on close', () => {
    calendarServiceSpy.getMonthView.calls.reset();
    component.openDayView({ date: '2026-03-01', eventColors: [], totalEvents: 0, pendingTasks: 0 });
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(calendarServiceSpy.getMonthView).toHaveBeenCalled();
  });

  it('openNewEventDialog should open dialog and reload on close', () => {
    calendarServiceSpy.getMonthView.calls.reset();
    component.openNewEventDialog();
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(calendarServiceSpy.getMonthView).toHaveBeenCalled();
  });

  it('previousMonth should decrement when not January', () => {
    component.currentMonth = 6;
    component.currentYear = 2026;

    component.previousMonth();

    expect(component.currentMonth).toBe(5);
    expect(component.currentYear).toBe(2026);
  });

  it('nextMonth should increment when not December', () => {
    component.currentMonth = 6;
    component.currentYear = 2026;

    component.nextMonth();

    expect(component.currentMonth).toBe(7);
    expect(component.currentYear).toBe(2026);
  });

  it('goToToday should set current month and year', () => {
    const today = new Date();
    component.currentMonth = 1;
    component.currentYear = 2000;

    component.goToToday();

    expect(component.currentMonth).toBe(today.getMonth() + 1);
    expect(component.currentYear).toBe(today.getFullYear());
  });

  it('getMonthName should return correct label', () => {
    component.currentMonth = 2;
    expect(component.getMonthName()).toBe('February');
  });

  it('getDayNumber should return null when day is missing', () => {
    expect(component.getDayNumber(null)).toBeNull();
  });

  it('isToday should return false for null day', () => {
    expect(component.isToday(null)).toBeFalse();
  });
});
