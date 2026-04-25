import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { TasksHeatmapComponent } from './tasks-heatmap.component';
import { CalendarService } from '../../../services/calendar.service';
import { CalendarMonthDTO } from '../../../dtos/calendar-month.dto';

describe('TasksHeatmapComponent', () => {
  let component: TasksHeatmapComponent;
  let fixture: ComponentFixture<TasksHeatmapComponent>;
  let mockCalendarService: jasmine.SpyObj<CalendarService>;

  const mockMonthData: CalendarMonthDTO = {
    year: 2026,
    month: 3,
    days: [
      { date: '2026-03-01', eventColors: [], totalEvents: 0, pendingTasks: 2 },
      { date: '2026-03-02', eventColors: [], totalEvents: 0, pendingTasks: 5 },
      { date: '2026-03-03', eventColors: [], totalEvents: 0, pendingTasks: 0 },
    ]
  };

  beforeEach(async () => {
    mockCalendarService = jasmine.createSpyObj('CalendarService', ['getMonthView']);

    await TestBed.configureTestingModule({
      declarations: [TasksHeatmapComponent],
      providers: [
        { provide: CalendarService, useValue: mockCalendarService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TasksHeatmapComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    mockCalendarService.getMonthView.and.returnValue(of(mockMonthData));
    expect(component).toBeTruthy();
  });

  it('should load current month data on init', () => {
    mockCalendarService.getMonthView.and.returnValue(of(mockMonthData));
    
    component.ngOnInit();

    expect(mockCalendarService.getMonthView).toHaveBeenCalled();
    expect(component.monthData).toEqual(mockMonthData);
    expect(component.loading).toBeFalse();
  });

  it('should calculate max tasks correctly', () => {
    component.monthData = mockMonthData;
    component.calculateMaxTasks();
    
    expect(component.maxTasks).toBe(5);
  });

  it('should keep maxTasks at 0 when no month data', () => {
    component.monthData = null;
    component.maxTasks = 0;
    component.calculateMaxTasks();
    expect(component.maxTasks).toBe(0);
  });

  it('should generate correct heatmap color based on task count', () => {
    component.maxTasks = 10;
    
    const day1 = { date: '2026-03-01', eventColors: [], totalEvents: 0, pendingTasks: 0 };
    const day2 = { date: '2026-03-02', eventColors: [], totalEvents: 0, pendingTasks: 2 };
    const day3 = { date: '2026-03-03', eventColors: [], totalEvents: 0, pendingTasks: 5 };
    const day4 = { date: '2026-03-04', eventColors: [], totalEvents: 0, pendingTasks: 10 };

    expect(component.getHeatmapColor(day1)).toBe('#f0f0f0');
    expect(component.getHeatmapColor(day2)).toBe('#f8bbd0');
    expect(component.getHeatmapColor(day3)).toBe('#f06292');
    expect(component.getHeatmapColor(day4)).toBe('#880e4f');
  });

  it('should return day number for valid day', () => {
    const day = { date: '2026-03-02', eventColors: [], totalEvents: 0, pendingTasks: 2 };
    expect(component.getDayNumber(day)).toBe(2);
  });

  it('should return 0 for pending tasks when day is null', () => {
    expect(component.getPendingTasks(null)).toBe(0);
  });

  it('should return false for isToday when day is null', () => {
    expect(component.isToday(null)).toBeFalse();
  });

  it('should handle service error gracefully', () => {
    mockCalendarService.getMonthView.and.returnValue(throwError(() => new Error('Service error')));
    
    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(component.monthData).toBeNull();
  });
});
