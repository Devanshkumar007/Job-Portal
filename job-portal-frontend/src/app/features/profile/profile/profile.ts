import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { AuthService, User } from '../../../core/services/auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  profileForm: FormGroup;
  user: User | null = null;
  isLoading = true;
  isSaving = false;
  isEditing = false;

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      phone: ['']
    });
  }

  ngOnInit() {
    this.user = this.authService.getCurrentUser();
    this.loadProfile();
  }

  onSubmit() {
    if (this.profileForm.invalid || !this.user) return;
    this.isSaving = true;
    this.http.put(`http://localhost:8080/api/user/me`, {
      name: this.profileForm.value.name,
      phone: this.profileForm.value.phone,
      email: this.user.email
    }).subscribe({
      next: (updated: any) => {
        const newUser = { ...this.user!, ...updated };
        localStorage.setItem('user', JSON.stringify(newUser));
        this.user = newUser;
        this.isSaving = false;
        this.isEditing = false;
        this.profileForm.patchValue({ name: newUser.name, phone: newUser.phone });
        this.snackBar.open('Profile updated!', 'Close', { duration: 3000 });
      },
      error: () => {
        this.isSaving = false;
        this.snackBar.open('Update failed.', 'Close', { duration: 3000 });
      }
    });
  }

  startEditing() {
    if (!this.user) return;
    this.isEditing = true;
    this.profileForm.patchValue({ name: this.user.name, phone: this.user.phone });
  }

  cancelEditing() {
    this.isEditing = false;
    if (!this.user) return;
    this.profileForm.patchValue({ name: this.user.name, phone: this.user.phone });
  }

  logout() { this.authService.logout(); }

  private loadProfile() {
    this.isLoading = true;
    this.http.get<User>('http://localhost:8080/api/user/me').subscribe({
      next: (user) => {
        this.user = user;
        localStorage.setItem('user', JSON.stringify(user));
        this.profileForm.patchValue({ name: user.name, phone: user.phone });
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load profile.', 'Close', { duration: 3000 });
      }
    });
  }
}
