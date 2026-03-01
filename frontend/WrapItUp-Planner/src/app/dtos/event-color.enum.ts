export enum EventColor {
  BLUE = 'BLUE',
  GREEN = 'GREEN',
  RED = 'RED',
  YELLOW = 'YELLOW',
  PURPLE = 'PURPLE'
}

export const EVENT_COLOR_HEX: { [key in EventColor]: string } = {
  [EventColor.BLUE]: '#3B82F6',
  [EventColor.GREEN]: '#10B981',
  [EventColor.RED]: '#EF4444',
  [EventColor.YELLOW]: '#F59E0B',
  [EventColor.PURPLE]: '#8B5CF6'
};
