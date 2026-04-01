import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../../core/services/job';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-post-job',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './post-job.html',
  styleUrl: './post-job.scss'
})
export class PostJob {
  postJobForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private jobService: JobService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.postJobForm = this.fb.group({
      title: ['', Validators.required],
      companyName: ['', Validators.required],
      location: ['', Validators.required],
      salary: ['', [Validators.required, Validators.min(0)]],
      experience: ['', [Validators.required, Validators.min(0)]],
      description: ['', [Validators.required, Validators.minLength(50)]],
    });
  }

  onSubmit() {
    if (this.postJobForm.invalid) return;

    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.isLoading = true;

    const jobData = {
      ...this.postJobForm.value,
      recruiterEmail: user.email
    };

    this.jobService.createJob(jobData).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Job posted successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/recruiter/dashboard']);
      },
      error: (err: any) => {
        this.isLoading = false;
        this.snackBar.open(err.error?.message || 'Failed to post job.', 'Close', { duration: 3000 });
      }
    });
  }
}