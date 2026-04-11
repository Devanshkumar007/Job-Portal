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
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService, User } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  profileForm: FormGroup;
  user: User | null = null;
  isLoading = true;
  isSaving = false;
  isEditing = false;
  viewedEmail: string | null = null;

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private userService: UserService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      name: ['', Validators.required],
      phone: ['']
    });
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      this.viewedEmail = params.get('email');
      this.user = this.authService.getCurrentUser();
      this.isEditing = false;
      this.loadProfile();
    });
  }

  onSubmit() {
    if (this.profileForm.invalid || !this.user || !this.canEditProfile) return;
    this.isSaving = true;
    this.userService.updateMyProfile({
      name: this.profileForm.value.name,
      phone: this.profileForm.value.phone,
      email: this.user.email
    }).subscribe({
      next: (updated) => {
        const newUser = { ...this.user!, ...updated };
        localStorage.setItem('user', JSON.stringify(newUser));
        this.user = newUser;
        this.isSaving = false;
        this.isEditing = false;
        this.profileForm.patchValue({ name: newUser.name, phone: newUser.phone });
        this.snackBar.open('Profile updated!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.isSaving = false;
        this.snackBar.open(
          getHttpErrorMessage(error, {
            defaultMessage: 'Unable to update your profile right now.',
            statusMessages: {
              400: 'Please check your profile details and try again.',
              404: 'Your profile could not be found.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  startEditing() {
    if (!this.user || !this.canEditProfile) return;
    this.isEditing = true;
    this.profileForm.patchValue({ name: this.user.name, phone: this.user.phone });
  }

  cancelEditing() {
    this.isEditing = false;
    if (!this.user) return;
    this.profileForm.patchValue({ name: this.user.name, phone: this.user.phone });
  }

  logout() { this.authService.logout(); }

  get isViewingOwnProfile(): boolean {
    return !this.viewedEmail;
  }

  get canEditProfile(): boolean {
    return this.isViewingOwnProfile;
  }

  private loadProfile() {
    this.isLoading = true;
    const request$ = this.viewedEmail
      ? this.userService.getUserByEmail(this.viewedEmail)
      : this.userService.getMyProfile();

    request$.subscribe({
      next: (user) => {
        this.user = user;
        if (this.isViewingOwnProfile) {
          localStorage.setItem('user', JSON.stringify(user));
        }
        this.profileForm.patchValue({ name: user.name, phone: user.phone });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(error, {
            defaultMessage: this.isViewingOwnProfile
              ? 'Unable to load your profile right now.'
              : 'Unable to load this user profile right now.',
            statusMessages: {
              404: 'Your profile could not be found.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }
}
