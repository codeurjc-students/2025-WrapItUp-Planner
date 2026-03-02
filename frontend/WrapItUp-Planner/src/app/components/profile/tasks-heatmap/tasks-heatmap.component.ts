import { Component, OnInit } from '@angular/core';
import { CalendarService } from '../../../services/calendar.service';
import { CalendarMonthDTO } from '../../../dtos/calendar-month.dto';
import { CalendarDaySummaryDTO } from '../../../dtos/calendar-day-summary.dto';

@Component({
  selector: 'app-tasks-heatmap',
  templateUrl: './tasks-heatmap.component.html',
  styleUrls: ['./tasks-heatmap.component.css']
})
export class TasksHeatmapComponent implements OnInit {
  monthData: CalendarMonthDTO | null = null;
  weeks: (CalendarDaySummaryDTO | null)[][] = [];
  weekDays: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  loading: boolean = true;
  currentMonth: string = '';
  maxTasks: number = 0;

  constructor(private calendarService: CalendarService) {}

  ngOnInit(): void {
    this.loadCurrentMonth();
  }

  loadCurrentMonth(): void {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;
    
    this.currentMonth = today.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

    this.calendarService.getMonthView(year, month).subscribe({
      next: (data) => {
        this.monthData = data;
        this.calculateMaxTasks();
        this.generateWeeks();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading month data:', err);
        this.loading = false;
      }
    });
  }

  calculateMaxTasks(): void {
    if (!this.monthData) return;
    this.maxTasks = Math.max(...this.monthData.days.map(day => day.pendingTasks), 1);
  }

  generateWeeks(): void {
    if (!this.monthData) return;

    const firstDay = new Date(this.monthData.days[0].date);
    const startDayOfWeek = firstDay.getDay();
    
    let week: (CalendarDaySummaryDTO | null)[] = [];
    
    for (let i = 0; i < startDayOfWeek; i++) {
      week.push(null);
    }

    this.monthData.days.forEach((day) => {
      week.push(day);
      if (week.length === 7) {
        this.weeks.push(week);
        week = [];
      }
    });

    if (week.length > 0) {
      while (week.length < 7) {
        week.push(null);
      }
      this.weeks.push(week);
    }
  }

  getHeatmapColor(day: CalendarDaySummaryDTO | null): string {
    if (!day || day.pendingTasks === 0) {
      return '#f0f0f0';
    }

    const intensity = Math.min(day.pendingTasks / this.maxTasks, 1);
    
    if (intensity <= 0.25) {
      return '#f8bbd0';
    } else if (intensity <= 0.5) {
      return '#f06292';
    } else if (intensity <= 0.75) {
      return '#e91e63';
    } else {
      return '#880e4f';
    }
  }

  getDayNumber(day: CalendarDaySummaryDTO | null): number | null {
    if (!day) return null;
    return new Date(day.date).getDate();
  }

  getPendingTasks(day: CalendarDaySummaryDTO | null): number {
    return day?.pendingTasks || 0;
  }

  isToday(day: CalendarDaySummaryDTO | null): boolean {
    if (!day) return false;
    const today = new Date();
    const dayDate = new Date(day.date);
    return dayDate.toDateString() === today.toDateString();
  }
}
