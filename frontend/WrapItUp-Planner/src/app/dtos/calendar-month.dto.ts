import { CalendarDaySummaryDTO } from './calendar-day-summary.dto';

export interface CalendarMonthDTO {
  year: number;
  month: number;
  days: CalendarDaySummaryDTO[];
}
