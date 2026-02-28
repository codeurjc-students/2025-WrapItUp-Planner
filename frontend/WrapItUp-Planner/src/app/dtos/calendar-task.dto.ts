export interface CalendarTaskDTO {
  id?: number;
  userId: number;
  title: string;
  description?: string;
  taskDate: string;
  completed: boolean;
  completedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}
