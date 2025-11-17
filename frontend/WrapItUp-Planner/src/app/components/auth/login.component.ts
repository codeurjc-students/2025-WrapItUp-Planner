import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  error: string | null = null;

  constructor(private authService: AuthService) { }

  onSubmit(): void {
    this.error = null;
    this.authService.login(this.username, this.password).subscribe({
      next: (res) => {
        // if server returns status field
        if (res && res.status && res.status !== 'SUCCESS') {
          this.error = res.message || 'Login failed';
          return;
        }
        // navigate or update UI as needed
        window.location.reload();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Login error';
      }
    });
  }
}
