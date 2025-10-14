import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { convertToParamMap, ActivatedRoute } from '@angular/router';
import { AINoteDetailComponent } from './notes-details.component'; 
import { AINoteService } from '../services/ainote.service';
import { AINoteDTO } from '../dtos/ainote.dto';

describe('AINoteDetailComponent', () => {
  let component: AINoteDetailComponent;
  let fixture: ComponentFixture<AINoteDetailComponent>;
  let mockService: jasmine.SpyObj<AINoteService>;

  beforeEach(async () => {
    mockService = jasmine.createSpyObj('AINoteService', ['getNoteById']);

    await TestBed.configureTestingModule({
      declarations: [AINoteDetailComponent],
      imports: [FormsModule],
      providers: [
        { provide: AINoteService, useValue: mockService },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: '1' }))
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AINoteDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch note from route param and display it', () => {
    const testNote: AINoteDTO = {
      id: 1,
      overview: 'Resumen general de la sesión de IA',
      summary: 'Este es el contenido detallado del resumen',
      jsonQuestions: '{"questions": ["¿Qué es IA?", "¿Cómo funciona?"]}',
      visibility: true,
      userId: 1
    };

    mockService.getNoteById.and.returnValue(of(testNote));

    component.ngOnInit();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('div')?.textContent).toContain('Id: 1');

    expect(mockService.getNoteById).toHaveBeenCalledWith(1);
  });
});