import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss'
})
export class ForgotPassword {
  forgotPasswordForm: FormGroup;
  isLoading = false;
  requestSent = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly snackBar: MatSnackBar,
    private readonly route: ActivatedRoute
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.route.queryParamMap.subscribe((params) => {
      const email = params.get('email')?.trim();
      if (email) {
        this.forgotPasswordForm.patchValue({ email });
      }
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      this.forgotPasswordForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.authService.forgotPassword(this.forgotPasswordForm.getRawValue()).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.requestSent = true;
        this.snackBar.open(response.message || 'Reset link request submitted.', 'Close', { duration: 4000 });
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to send a reset link right now.',
            statusMessages: {
              400: 'Please enter a valid email address.'
            }
          }),
          'Close',
          { duration: 3500 }
        );
      }
    });
  }
}
