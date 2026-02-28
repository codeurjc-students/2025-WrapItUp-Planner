import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CalendarService } from '../../services/calendar.service';
import { CalendarDayDTO } from '../../dtos/calendar-day.dto';
import { CalendarEventDTO } from '../../dtos/calendar-event.dto';
import { CalendarTaskDTO } from '../../dtos/calendar-task.dto';
import { EventColor, EVENT_COLOR_HEX } from '../../dtos/event-color.enum';

@Component({
  selector: 'app-day-view-dialog',
  templateUrl: './day-view-dialog.component.html',
  styleUrls: ['./day-view-dialog.component.css']
})
export class DayViewDialogComponent implements OnInit {
  dayData: CalendarDayDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  success: string | null = null;

  showEventForm: boolean = false;
  showTaskForm: boolean = false;
  editingEvent: CalendarEventDTO | null = null;
  editingTask: CalendarTaskDTO | null = null;

  startDate: string = '';
  endDate: string = '';
  startTime: string = '09:00';
  endTime: string = '10:00';

  newEvent: Partial<CalendarEventDTO> = {
    title: '',
    description: '',
    allDay: true,
    color: EventColor.BLUE
  };

  newTask: Partial<CalendarTaskDTO> = {
    title: '',
    description: ''
  };

  eventColors = Object.values(EventColor);
  eventColorHex = EVENT_COLOR_HEX;

  constructor(
    public dialogRef: MatDialogRef<DayViewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { year: number; month: number; day: number; date: string },
    private calendarService: CalendarService
  ) {}

  ngOnInit(): void {
    this.loadDayData();
  }

  loadDayData(): void {
    this.loading = true;
    this.error = null;

    this.calendarService.getDayView(this.data.year, this.data.month, this.data.day).subscribe({
      next: (data) => {
        this.dayData = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading day data:', err);
        this.error = 'Error loading day data';
        this.loading = false;
      }
    });
  }

  getDateString(): string {
    const date = new Date(this.data.year, this.data.month - 1, this.data.day);
    return date.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  }

  openEventForm(): void {
    this.showEventForm = true;
    this.showTaskForm = false;
    this.editingEvent = null;
    this.startDate = this.data.date;
    this.endDate = this.data.date;
    this.startTime = '09:00';
    this.endTime = '10:00';
    this.newEvent = {
      title: '',
      description: '',
      allDay: true,
      color: EventColor.BLUE
    };
  }

  openTaskForm(): void {
    this.showTaskForm = true;
    this.showEventForm = false;
    this.editingTask = null;
    this.newTask = {
      title: '',
      description: ''
    };
  }

  editEvent(event: CalendarEventDTO): void {
    this.editingEvent = event;
    this.newEvent = { ...event };
    
    
    this.startDate = event.startDate.split('T')[0];
    this.endDate = event.endDate.split('T')[0];
    
    if (!event.allDay) {
      const start = new Date(event.startDate);
      const end = new Date(event.endDate);
      this.startTime = `${String(start.getHours()).padStart(2, '0')}:${String(start.getMinutes()).padStart(2, '0')}`;
      this.endTime = `${String(end.getHours()).padStart(2, '0')}:${String(end.getMinutes()).padStart(2, '0')}`;
    } else {
      this.startTime = '09:00';
      this.endTime = '10:00';
    }
    
    this.showEventForm = true;
    this.showTaskForm = false;
  }

  editTask(task: CalendarTaskDTO): void {
    this.editingTask = task;
    this.newTask = { ...task };
    this.showTaskForm = true;
    this.showEventForm = false;
  }

  cancelForm(): void {
    this.showEventForm = false;
    this.showTaskForm = false;
    this.editingEvent = null;
    this.editingTask = null;
    this.error = null;
    this.success = null;
  }

  saveEvent(): void {
    if (!this.newEvent.title || this.newEvent.title.trim() === '') {
      this.error = 'Event title is required';
      return;
    }

    let startDateTime: string;
    let endDateTime: string;

    if (this.newEvent.allDay) {
      startDateTime = this.startDate + 'T00:00:00';
      endDateTime = this.endDate + 'T23:59:59';
    } else {
      startDateTime = this.startDate + 'T' + this.startTime + ':00';
      endDateTime = this.endDate + 'T' + this.endTime + ':00';
    }

    const eventToSave: CalendarEventDTO = {
      userId: 0,
      title: this.newEvent.title!,
      description: this.newEvent.description || '',
      startDate: startDateTime,
      endDate: endDateTime,
      allDay: this.newEvent.allDay!,
      color: this.newEvent.color!
    };

    if (this.editingEvent && this.editingEvent.id) {
      this.calendarService.updateEvent(this.editingEvent.id, eventToSave).subscribe({
        next: () => {
          this.success = 'Event updated successfully';
          this.loadDayData();
          this.cancelForm();
          setTimeout(() => this.success = null, 3000);
        },
        error: (err) => {
          console.error('Error updating event:', err);
          this.error = err.error?.message || 'Error updating event';
        }
      });
    } else {
      this.calendarService.createEvent(eventToSave).subscribe({
        next: () => {
          this.success = 'Event created successfully';
          this.loadDayData();
          this.cancelForm();
          setTimeout(() => this.success = null, 3000);
        },
        error: (err) => {
          console.error('Error creating event:', err);
          this.error = err.error?.message || 'Error creating event';
        }
      });
    }
  }

  saveTask(): void {
    if (!this.newTask.title || this.newTask.title.trim() === '') {
      this.error = 'Task title is required';
      return;
    }

    const taskToSave: CalendarTaskDTO = {
      userId: 0,
      title: this.newTask.title!,
      description: this.newTask.description || '',
      taskDate: this.data.date,
      completed: this.newTask.completed || false
    };

    if (this.editingTask && this.editingTask.id) {
      this.calendarService.updateTask(this.editingTask.id, taskToSave).subscribe({
        next: () => {
          this.success = 'Task updated successfully';
          this.loadDayData();
          this.cancelForm();
          setTimeout(() => this.success = null, 3000);
        },
        error: (err) => {
          console.error('Error updating task:', err);
          this.error = err.error?.message || 'Error updating task';
        }
      });
    } else {
      this.calendarService.createTask(taskToSave).subscribe({
        next: () => {
          this.success = 'Task created successfully';
          this.loadDayData();
          this.cancelForm();
          setTimeout(() => this.success = null, 3000);
        },
        error: (err) => {
          console.error('Error creating task:', err);
          this.error = err.error?.message || 'Error creating task';
        }
      });
    }
  }

  toggleTask(task: CalendarTaskDTO): void {
    if (!task.id) return;
    
    this.calendarService.toggleTaskComplete(task.id).subscribe({
      next: () => {
        this.loadDayData();
      },
      error: (err) => {
        console.error('Error toggling task:', err);
        this.error = 'Error updating task';
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  deleteEvent(event: CalendarEventDTO): void {
    if (!event.id) return;
    
    if (!confirm('Are you sure you want to delete this event?')) return;

    this.calendarService.deleteEvent(event.id).subscribe({
      next: () => {
        this.success = 'Event deleted successfully';
        this.loadDayData();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        console.error('Error deleting event:', err);
        this.error = 'Error deleting event';
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  deleteTask(task: CalendarTaskDTO): void {
    if (!task.id) return;
    
    if (!confirm('Are you sure you want to delete this task?')) return;

    this.calendarService.deleteTask(task.id).subscribe({
      next: () => {
        this.success = 'Task deleted successfully';
        this.loadDayData();
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        console.error('Error deleting task:', err);
        this.error = 'Error deleting task';
        setTimeout(() => this.error = null, 3000);
      }
    });
  }

  close(): void {
    this.dialogRef.close('refresh');
  }
}
