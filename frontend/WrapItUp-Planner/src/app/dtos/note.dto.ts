export interface NoteDTO {
  id?: number;
  title?: string;
  overview?: string;
  summary?: string;
  jsonQuestions?: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  userId?: number;
  sharedWithUserIds?: number[];
}
