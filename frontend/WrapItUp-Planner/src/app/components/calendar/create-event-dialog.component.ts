import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { CalendarService } from '../../services/calendar.service';
import { CalendarEventDTO } from '../../dtos/calendar-event.dto';
import { EventColor, EVENT_COLOR_HEX } from '../../dtos/event-color.enum';

@Component({
  selector: 'app-create-event-dialog',
  templateUrl: './create-event-dialog.component.html',
  styleUrls: ['./create-event-dialog.component.css']
})
export class CreateEventDialogComponent implements OnInit {
  loading: boolean = false;
  error: string | null = null;
  success: string | null = null;

  startDate: string = '';
  endDate: string = '';
  startTime: string = '09:00';
  endTime: string = '10:00';
  allDay: boolean = false;

  newEvent: Partial<CalendarEventDTO> = {
    title: '',
    description: '',
    color: EventColor.BLUE
  };

  eventColors = Object.values(EventColor);
  eventColorHex = EVENT_COLOR_HEX;

  constructor(
    public dialogRef: MatDialogRef<CreateEventDialogComponent>,
    private calendarService: CalendarService
  ) {}

  ngOnInit(): void {
    const today = new Date();
    this.startDate = this.formatDate(today);
    
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.endDate = this.formatDate(tomorrow);
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  saveEvent(): void {
    if (!this.newEvent.title || this.newEvent.title.trim() === '') {
      this.error = 'Event title is required';
      return;
    }

    if (!this.startDate || !this.endDate) {
      this.error = 'Start and end dates are required';
      return;
    }

    let startDateTime: string;
    let endDateTime: string;

    if (this.allDay) {
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
      allDay: this.allDay,
      color: this.newEvent.color!
    };

    this.loading = true;
    this.calendarService.createEvent(eventToSave).subscribe({
      next: () => {
        this.success = 'Event created successfully';
        setTimeout(() => {
          this.dialogRef.close('refresh');
        }, 1000);
      },
      error: (err) => {
        console.error('Error creating event:', err);
        this.error = err.error?.message || 'Error creating event';
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
