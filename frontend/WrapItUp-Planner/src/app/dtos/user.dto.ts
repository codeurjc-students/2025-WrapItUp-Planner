export interface UserModelDTO {
  id?: number;
  username: string;
  email: string;
  password: string;
  roles?: string[];
  status?: string;
}
