import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserModelDTO } from '../../dtos/user.dto';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
selector: 'app-profile',
templateUrl: './profile.component.html',
styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: UserModelDTO | null = null;
  editedUser: UserModelDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  success: string | null = null;
  isEditing: boolean = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserData();
  }

  loadUserData(): void {
    this.userService.getCurrentUser().subscribe({
      next: (userData) => {
        this.user = userData;
        this.editedUser = { ...userData };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading user data';
        this.loading = false;
        console.error('Error loading user:', err);
      }
    });
  }

  toggleEdit(): void {
    if (this.isEditing) {
      // Cancel editing
      this.editedUser = this.user ? { ...this.user } : null;
      this.error = null;
      this.success = null;
      this.selectedFile = null;
      this.imagePreview = null;
    }
    this.isEditing = !this.isEditing;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        this.error = 'Please select an image file';
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.error = 'Image size must be less than 5MB';
        return;
      }

      this.selectedFile = file;
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Upload immediately
      this.uploadImage();
    }
  }

  uploadImage(): void {
    if (!this.selectedFile) return;

    this.error = null;
    
    this.userService.uploadProfileImage(this.selectedFile).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.editedUser = { ...updatedUser };
        this.success = 'Image uploaded successfully';
        setTimeout(() => this.success = null, 3000);
        this.selectedFile = null;
      },
      error: (err) => {
        this.error = err?.error || 'Error uploading image';
        this.selectedFile = null;
        this.imagePreview = null;
      }
    });
  }

  saveChanges(): void {
    if (!this.editedUser) return;

    this.error = null;
    this.success = null;

    // Validations
    // Display name is optional - if empty, it will keep the current value
    if (!this.editedUser.email || this.editedUser.email.trim() === '') {
      this.error = 'Email is required';
      return;
    }

    // Email format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editedUser.email)) {
      this.error = 'Invalid email format';
      return;
    }

    this.userService.updateUser(this.editedUser).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.editedUser = { ...updatedUser };
        this.isEditing = false;
        this.success = 'Profile updated successfully';
        setTimeout(() => this.success = null, 3000);
      },
      error: (err) => {
        this.error = err?.error || 'Error updating profile';
      }
    });
  }

  getRoleName(): string {
    if (!this.user?.roles || this.user.roles.length === 0) {
      return 'User';
    }
    return this.user.roles[0];
  }

  isAdmin(): boolean {
    return this.user?.roles?.includes('ADMIN') ?? false;
  }

  getImageUrl(): string {
    if (this.imagePreview) {
      return this.imagePreview;
    }
    if (this.user?.image) {
      return 'https://localhost:443' + this.user.image;
    }
    return 'assets/genericUser.png';
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Error during logout:', err);
        // Navigate anyway
        this.router.navigate(['/login']);
      }
    });
  }
}