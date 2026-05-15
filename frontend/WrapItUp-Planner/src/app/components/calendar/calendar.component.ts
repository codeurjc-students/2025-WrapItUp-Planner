import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarService } from '../../services/calendar.service';
import { CalendarMonthDTO } from '../../dtos/calendar-month.dto';
import { CalendarDaySummaryDTO } from '../../dtos/calendar-day-summary.dto';
import { DayViewDialogComponent } from './day-view-dialog.component';
import { CreateEventDialogComponent } from './create-event-dialog.component';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;
  monthData: CalendarMonthDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  
  weekDays: string[] = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  
  calendarDays: (CalendarDaySummaryDTO | null)[][] = [];

  constructor(
    private calendarService: CalendarService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadMonth();
  }

  loadMonth(): void {
    this.loading = true;
    this.error = null;
    
    this.calendarService.getMonthView(this.currentYear, this.currentMonth).subscribe({
      next: (data) => {
        this.monthData = data;
        this.buildCalendarGrid();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading calendar:', err);
        this.error = 'Error loading calendar data';
        this.loading = false;
      }
    });
  }

  buildCalendarGrid(): void {
    if (!this.monthData) return;

    const firstDayOfMonth = new Date(this.currentYear, this.currentMonth - 1, 1);
    let firstDayWeekday = firstDayOfMonth.getDay();
    firstDayWeekday = firstDayWeekday === 0 ? 7 : firstDayWeekday;
    
    const daysInMonth = new Date(this.currentYear, this.currentMonth, 0).getDate();
    
    this.calendarDays = [];
    let week: (CalendarDaySummaryDTO | null)[] = [];
    
    for (let i = 1; i < firstDayWeekday; i++) {
      week.push(null);
    }
    
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const daySummary = this.monthData.days.find(d => d.date === dateStr);
      
      week.push(daySummary || {
        date: dateStr,
        eventColors: [],
        totalEvents: 0,
        pendingTasks: 0
      });
      
      if (week.length === 7) {
        this.calendarDays.push(week);
        week = [];
      }
    }
    
    while (week.length > 0 && week.length < 7) {
      week.push(null);
    }
    if (week.length > 0) {
      this.calendarDays.push(week);
    }
  }

  openDayView(daySummary: CalendarDaySummaryDTO | null): void {
    if (!daySummary) return;
    
    const date = new Date(daySummary.date);
    const dialogRef = this.dialog.open(DayViewDialogComponent, {
      width: '1000px',
      maxWidth: '95vw',
      maxHeight: '90vh',
      data: {
        year: date.getFullYear(),
        month: date.getMonth() + 1,
        day: date.getDate(),
        date: daySummary.date
      }
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadMonth();
    });
  }

  openNewEventDialog(): void {
    const dialogRef = this.dialog.open(CreateEventDialogComponent, {
      width: '750px',
      maxWidth: '95vw',
      maxHeight: '90vh'
    });

    dialogRef.afterClosed().subscribe(() => {
      this.loadMonth();
    });
  }

  previousMonth(): void {
    if (this.currentMonth === 1) {
      this.currentMonth = 12;
      this.currentYear--;
    } else {
      this.currentMonth--;
    }
    this.loadMonth();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) {
      this.currentMonth = 1;
      this.currentYear++;
    } else {
      this.currentMonth++;
    }
    this.loadMonth();
  }

  goToToday(): void {
    const today = new Date();
    this.currentYear = today.getFullYear();
    this.currentMonth = today.getMonth() + 1;
    this.loadMonth();
  }

  getMonthName(): string {
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'];
    return monthNames[this.currentMonth - 1];
  }

  getDayNumber(daySummary: CalendarDaySummaryDTO | null): number | null {
    if (!daySummary) return null;
    return new Date(daySummary.date).getDate();
  }

  isToday(daySummary: CalendarDaySummaryDTO | null): boolean {
    if (!daySummary) return false;
    const today = new Date();
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    return daySummary.date === todayStr;
  }
}
