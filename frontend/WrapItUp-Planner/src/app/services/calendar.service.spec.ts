import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { CalendarService } from './calendar.service';
import { EventColor } from '../dtos/event-color.enum';
import { CalendarEventDTO } from '../dtos/calendar-event.dto';
import { CalendarTaskDTO } from '../dtos/calendar-task.dto';
import { CalendarMonthDTO } from '../dtos/calendar-month.dto';
import { CalendarDayDTO } from '../dtos/calendar-day.dto';

describe('CalendarService', () => {
  let service: CalendarService;
  let httpMock: HttpTestingController;

  const BASE_URL = 'https://localhost:443/api/v1/calendar';
  const EVENTS_URL = `${BASE_URL}/events`;
  const TASKS_URL = `${BASE_URL}/tasks`;

  const mockEvent: CalendarEventDTO = {
    id: 1,
    userId: 1,
    title: 'Test Event',
    description: 'Description',
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

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CalendarService]
    });
    service = TestBed.inject(CalendarService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMonthView should GET /month/:year/:month', () => {
    const mockMonth: CalendarMonthDTO = { year: 2026, month: 3, days: [] };
    service.getMonthView(2026, 3).subscribe(data => expect(data).toEqual(mockMonth));
    const req = httpMock.expectOne(`${BASE_URL}/month/2026/3`);
    expect(req.request.method).toBe('GET');
    req.flush(mockMonth);
  });

  it('getDayView should GET /day/:year/:month/:day', () => {
    service.getDayView(2026, 3, 1).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/day/2026/3/1`);
    expect(req.request.method).toBe('GET');
    req.flush({ date: '2026-03-01', events: [], tasks: [] });
  });

  it('createEvent should POST to events endpoint with body', () => {
    const newEvent: CalendarEventDTO = { userId: 1, title: 'New', startDate: '2026-03-01T09:00:00', endDate: '2026-03-01T10:00:00', allDay: false, color: EventColor.GREEN };
    service.createEvent(newEvent).subscribe();
    const req = httpMock.expectOne(EVENTS_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newEvent);
    req.flush({ ...newEvent, id: 1 });
  });

  it('updateEvent should PUT to events/:id', () => {
    service.updateEvent(1, mockEvent).subscribe();
    const req = httpMock.expectOne(`${EVENTS_URL}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockEvent);
  });

  it('deleteEvent should DELETE events/:id', () => {
    service.deleteEvent(1).subscribe();
    const req = httpMock.expectOne(`${EVENTS_URL}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('createTask should POST to tasks endpoint', () => {
    const newTask: CalendarTaskDTO = { userId: 1, title: 'New Task', taskDate: '2026-03-01', completed: false };
    service.createTask(newTask).subscribe();
    const req = httpMock.expectOne(TASKS_URL);
    expect(req.request.method).toBe('POST');
    req.flush({ ...newTask, id: 1 });
  });

  it('toggleTaskComplete should PATCH tasks/:id/toggle', () => {
    service.toggleTaskComplete(1).subscribe(data => expect(data.completed).toBeTrue());
    const req = httpMock.expectOne(`${TASKS_URL}/1/toggle`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ...mockTask, completed: true });
  });

  it('getTasks with pendingOnly should include param', () => {
    service.getTasks(undefined, undefined, undefined, true).subscribe();
    const req = httpMock.expectOne(r => r.url === TASKS_URL);
    expect(req.request.params.get('pendingOnly')).toBe('true');
    req.flush([]);
  });
});
