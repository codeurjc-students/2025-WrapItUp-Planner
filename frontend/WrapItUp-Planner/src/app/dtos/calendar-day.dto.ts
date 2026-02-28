import { CalendarEventDTO } from './calendar-event.dto';
import { CalendarTaskDTO } from './calendar-task.dto';

export interface CalendarDayDTO {
  date: string;
  events: CalendarEventDTO[];
  tasks: CalendarTaskDTO[];
}
