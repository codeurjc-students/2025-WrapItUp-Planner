import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserModelDTO } from '../../dtos/user.dto';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent implements OnInit {
  mode: 'login' | 'register' = 'login';

  // shared fields
  username = '';
  password = '';
  error: string | null = null;
  message: string | null = null;

  // register specific
  model: UserModelDTO = { username: '', email: '', password: '' };
  repeatPassword = '';

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.setModeFromUrl();
    // update mode on navigation
    this.router.events.subscribe(() => this.setModeFromUrl());
  }

  private setModeFromUrl(): void {
    const url = this.router.url || '';
    if (url.includes('/register')) {
      this.mode = 'register';
    } else {
      this.mode = 'login';
    }
    // reset messages when mode changes
    this.error = null;
    this.message = null;
  }

  onSubmit(): void {
    this.error = null;
    this.message = null;

    if (this.mode === 'login') {
      if (!this.username || !this.password) {
        this.error = 'Username and password are required';
        return;
      }
      this.authService.login(this.username, this.password).subscribe({
        next: (res) => {
          if (res && res.status && res.status !== 'SUCCESS') {
            this.error = 'Bad credentials';
            return;
          }
          // successful login - wait a moment for cookies to be set before redirecting
          setTimeout(() => {
            this.router.navigate(['/profile']);
          }, 300);
        },
        error: (err) => {
          this.error = 'Bad credentials';
        }
      });
    } else {
      // register
      if (!this.model.username || !this.model.email || !this.model.password) {
        this.error = 'All fields are required';
        return;
      }
      if (this.model.password !== this.repeatPassword) {
        this.error = 'Passwords do not match';
        return;
      }
      if (this.model.password.length < 8) {
        this.error = 'Password must be at least 8 characters';
        return;
      }

      this.authService.register(this.model).subscribe({
        next: (res) => {
          this.message = res?.message || 'Registered successfully';
          // redirect to login
          this.router.navigate(['/login']);
        },
        error: (err) => {
          const serverError = err?.error?.error || err?.error?.message || '';
          if (serverError.toLowerCase().includes('already exists')) {
            this.error = serverError;
          } else {
            this.error = 'Registration error. Please try again.';
          }
        }
      });
    }
  }
}
