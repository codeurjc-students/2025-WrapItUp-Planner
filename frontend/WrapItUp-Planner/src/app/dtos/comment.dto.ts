export interface CommentDTO {
  id?: number;
  content: string;
  noteId?: number;
  userId?: number;
  username?: string;
  displayName?: string;
  userProfilePicUrl?: string;
  createdAt?: string;
  isReported?: boolean;
}
