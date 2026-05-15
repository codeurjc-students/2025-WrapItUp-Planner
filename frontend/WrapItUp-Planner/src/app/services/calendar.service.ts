import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CalendarEventDTO } from '../dtos/calendar-event.dto';
import { CalendarTaskDTO } from '../dtos/calendar-task.dto';
import { CalendarDayDTO } from '../dtos/calendar-day.dto';
import { CalendarMonthDTO } from '../dtos/calendar-month.dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {

  private apiUrl = `${environment.apiUrl}/api/v1/calendar`;
  private eventsUrl = `${environment.apiUrl}/api/v1/calendar/events`;
  private tasksUrl = `${environment.apiUrl}/api/v1/calendar/tasks`;

  constructor(private http: HttpClient) { }

  getMonthView(year: number, month: number): Observable<CalendarMonthDTO> {
    return this.http.get<CalendarMonthDTO>(`${this.apiUrl}/month/${year}/${month}`, { withCredentials: true });
  }

  getDayView(year: number, month: number, day: number): Observable<CalendarDayDTO> {
    return this.http.get<CalendarDayDTO>(`${this.apiUrl}/day/${year}/${month}/${day}`, { withCredentials: true });
  }

  getDayViewByDate(date: string): Observable<CalendarDayDTO> {
    return this.http.get<CalendarDayDTO>(`${this.apiUrl}/day`, {
      params: { date },
      withCredentials: true
    });
  }

  createEvent(event: CalendarEventDTO): Observable<CalendarEventDTO> {
    return this.http.post<CalendarEventDTO>(this.eventsUrl, event, { withCredentials: true });
  }

  updateEvent(id: number, event: CalendarEventDTO): Observable<CalendarEventDTO> {
    return this.http.put<CalendarEventDTO>(`${this.eventsUrl}/${id}`, event, { withCredentials: true });
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.eventsUrl}/${id}`, { withCredentials: true });
  }

  getEvents(startDate?: string, endDate?: string): Observable<CalendarEventDTO[]> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<CalendarEventDTO[]>(this.eventsUrl, { params, withCredentials: true });
  }

  createTask(task: CalendarTaskDTO): Observable<CalendarTaskDTO> {
    return this.http.post<CalendarTaskDTO>(this.tasksUrl, task, { withCredentials: true });
  }

  updateTask(id: number, task: CalendarTaskDTO): Observable<CalendarTaskDTO> {
    return this.http.put<CalendarTaskDTO>(`${this.tasksUrl}/${id}`, task, { withCredentials: true });
  }

  toggleTaskComplete(id: number): Observable<CalendarTaskDTO> {
    return this.http.patch<CalendarTaskDTO>(`${this.tasksUrl}/${id}/toggle`, {}, { withCredentials: true });
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.tasksUrl}/${id}`, { withCredentials: true });
  }

  getTasks(date?: string, startDate?: string, endDate?: string, pendingOnly?: boolean): Observable<CalendarTaskDTO[]> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    if (pendingOnly) params = params.set('pendingOnly', 'true');
    return this.http.get<CalendarTaskDTO[]>(this.tasksUrl, { params, withCredentials: true });
  }
}
