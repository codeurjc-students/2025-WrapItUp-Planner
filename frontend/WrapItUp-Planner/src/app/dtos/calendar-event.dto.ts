import { EventColor } from './event-color.enum';

export interface CalendarEventDTO {
  id?: number;
  userId: number;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  allDay: boolean;
  color: EventColor;
  colorHex?: string;
  createdAt?: string;
  updatedAt?: string;
}
