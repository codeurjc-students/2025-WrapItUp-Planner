export type NoteCategory = 'MATHS' | 'SCIENCE' | 'HISTORY' | 'ART' | 'LANGUAGES' | 'OTHERS';

export interface NoteDTO {
  id?: number;
  title?: string;
  overview?: string;
  summary?: string;
  jsonQuestions?: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  category?: NoteCategory;
  lastModified?: string;
  userId?: number;
  sharedWithUserIds?: number[];
}
