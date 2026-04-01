import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../../core/services/job';
import { ApplicationService } from '../../../core/services/application';
import { AuthService, Job } from '../../../core/services/auth';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './job-detail.html',
  styleUrl: './job-detail.scss'
})
export class JobDetail implements OnInit {
  job: Job | null = null;
  isLoading = true;
  isApplying = false;
  hasApplied = false;
  selectedFile: File | null = null;
  showApplyForm = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private jobService: JobService,
    private applicationService: ApplicationService,
    public authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.jobService.getJobById(Number(id)).subscribe({
        next: (job) => { this.job = job; this.isLoading = false; },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Job not found!', 'Close', { duration: 3000 });
          this.router.navigate(['/jobs']);
        }
      });
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        this.snackBar.open('Only PDF files are allowed!', 'Close', { duration: 3000 });
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        this.snackBar.open('File size must be less than 5MB!', 'Close', { duration: 3000 });
        return;
      }
      this.selectedFile = file;
    }
  }

  applyForJob() {
    if (!this.job || !this.selectedFile) {
      this.snackBar.open('Please upload your resume first!', 'Close', { duration: 3000 });
      return;
    }

    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.isApplying = true;

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('jobId', this.job.id.toString());
    formData.append('applicantEmail', user.email);
    formData.append('jobTitle', this.job.title);
    formData.append('company', this.job.companyName);

    this.applicationService.applyForJobWithForm(formData).subscribe({
      next: () => {
        this.isApplying = false;
        this.hasApplied = true;
        this.showApplyForm = false;
        this.snackBar.open('Application submitted successfully!', 'Close', { duration: 3000 });
      },
      error: (err: any) => {
        this.isApplying = false;
        this.snackBar.open(err.error?.message || 'Failed to apply. Please try again.', 'Close', { duration: 3000 });
      }
    });
  }

  formatSalary(salary: number): string {
    return `₹${(salary / 100000).toFixed(1)} LPA`;
  }

  goBack() { this.router.navigate(['/jobs']); }
}