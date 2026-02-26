import { UserStatus } from './user-status.enum';

export interface UserModelDTO {
  id?: number;
  username: string;
  displayName?: string;
  email: string;
  password: string;
  image?: string;
  roles?: string[];
  status?: UserStatus;
}
