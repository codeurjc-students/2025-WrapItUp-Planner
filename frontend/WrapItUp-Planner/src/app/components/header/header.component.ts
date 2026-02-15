import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { UserService } from '../../services/user.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  isAuthenticated: boolean = false;
  isAdmin: boolean = false;

  constructor(
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Check authentication on init
    this.checkAuthentication();

    // Check authentication on every navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkAuthentication();
    });
  }

  checkAuthentication(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.isAuthenticated = !!user;
        this.isAdmin = user?.roles?.includes('ADMIN') ?? false;
      },
      error: () => {
        this.isAuthenticated = false;
        this.isAdmin = false;
      }
    });
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
