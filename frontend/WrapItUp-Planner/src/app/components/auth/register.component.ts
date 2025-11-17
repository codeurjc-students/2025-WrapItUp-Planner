import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserModelDTO } from '../../dtos/user.dto';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  model: UserModelDTO = { username: '', email: '', password: '' };
  message: string | null = null;
  error: string | null = null;

  constructor(private authService: AuthService) { }

  onSubmit(): void {
    this.error = null;
    this.message = null;
    this.authService.register(this.model).subscribe({
      next: (res) => {
        this.message = res?.message || 'Registered successfully';
      },
      error: (err) => {
        this.error = err?.error?.error || err?.error?.message || 'Register error';
      }
    });
  }
}
