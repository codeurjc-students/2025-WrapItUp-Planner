import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { convertToParamMap, ActivatedRoute } from '@angular/router';
import { NoteDetailComponent } from './notes-details.component'; 
import { NoteService } from '../services/note.service';
import { NoteDTO } from '../dtos/note.dto';

describe('NoteDetailComponent', () => {
  let component: NoteDetailComponent;
  let fixture: ComponentFixture<NoteDetailComponent>;
  let mockService: jasmine.SpyObj<NoteService>;

  beforeEach(async () => {
    mockService = jasmine.createSpyObj('NoteService', ['getNoteById']);

    await TestBed.configureTestingModule({
      declarations: [NoteDetailComponent],
      imports: [FormsModule],
      providers: [
        { provide: NoteService, useValue: mockService },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '1' }))
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NoteDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch note from route param and display it', () => {
    const testNote: NoteDTO = {
      id: 1,
      overview: 'Resumen general de la sesión',
      summary: 'Este es el contenido detallado del resumen',
      jsonQuestions: '{"questions": ["¿Qué es esto?", "¿Cómo funciona?"]}',
      visibility: true,
      userId: 1
    };

    mockService.getNoteById.and.returnValue(of(testNote));

    component.ngOnInit();
    fixture.detectChanges();

    expect(mockService.getNoteById).toHaveBeenCalledWith(1);
    expect(component.note).toEqual(testNote);
    
    const compiled = fixture.nativeElement as HTMLElement;
    const idParagraph = compiled.querySelector('p');
    expect(idParagraph?.textContent).toContain('Id: 1');
  });
});