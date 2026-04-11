import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../../core/services/job';
import { ApplicationService } from '../../../core/services/application';
import { AuthService, Job, Application } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
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
  isDeleting = false;
  hasApplied = false;
  applicationStatus: Application | null = null;
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
        next: (job) => {
          this.job = job;
          this.isLoading = false;
          // Fetch application status for job seeker
          if (this.authService.isJobSeeker()) {
            this.loadApplicationStatus(Number(id));
          }
        },
        error: () => {
          this.isLoading = false;
          this.snackBar.open('Job not found!', 'Close', { duration: 3000 });
          this.router.navigate(['/jobs']);
        }
      });
    }
  }

  loadApplicationStatus(jobId: number): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.applicationService.getMyApplications(user.id, 0, 100).subscribe({
      next: (response) => {
        const application = response.content.find(app => app.jobId === jobId);
        if (application) {
          this.applicationStatus = application;
          this.hasApplied = true;
        }
      },
      error: () => {
        // Silently fail - application status not found is acceptable
      }
    });
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
    formData.append('applicantName', user.name);
    formData.append('applicantEmail', user.email);
    formData.append('jobTitle', this.job.title);
    formData.append('company', this.job.companyName);

    this.applicationService.applyForJobWithForm(formData).subscribe({
      next: (application) => {
        this.isApplying = false;
        this.hasApplied = true;
        this.applicationStatus = application;
        this.showApplyForm = false;
        this.snackBar.open('Application submitted successfully!', 'Close', { duration: 3000 });
      },
      error: (err: any) => {
        this.isApplying = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to submit your application right now.',
            statusMessages: {
              400: 'Please check your application details and resume, then try again.',
              404: 'This job is no longer available.',
              409: 'You have already applied for this job.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  formatSalary(job: Job): string {
    if (job.jobType === 'INTERNSHIP') {
      return `₹${Math.round(job.salary).toLocaleString('en-IN')}/month`;
    }
    return `₹${(job.salary / 100000).toFixed(1)} LPA`;
  }

  canRecruiterManageJob(): boolean {
    if (!this.job || !this.authService.isRecruiter()) {
      return false;
    }

    const user = this.authService.getCurrentUser();
    return user?.id === this.job.recruiterId;
  }

  deleteJob(): void {
    if (!this.job || !this.authService.isAdmin()) {
      return;
    }

    const confirmed = confirm('Are you sure you want to delete this job? This action cannot be undone.');
    if (!confirmed) {
      return;
    }

    this.isDeleting = true;
    this.jobService.deleteJobAsAdmin(this.job.id).subscribe({
      next: () => {
        this.isDeleting = false;
        this.snackBar.open('Job deleted successfully!', 'Close', { duration: 3000 });
        this.router.navigate(['/admin/jobs']);
      },
      error: (err) => {
        this.isDeleting = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to delete this job right now.',
            statusMessages: {
              403: 'You do not have permission to delete this job.',
              404: 'This job no longer exists.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  editJob(): void {
    if (!this.job || !this.canRecruiterManageJob()) {
      return;
    }

    this.router.navigate(['/recruiter/jobs', this.job.id, 'edit']);
  }

  goBack() { this.router.navigate(['/jobs']); }

  viewAllApplications() {
    if (!this.job) {
      return;
    }
    this.router.navigate(['/recruiter/applications'], { queryParams: { jobId: this.job.id } });
  }

  getStatusIcon(status: string): string {
    const map: any = {
      APPLIED: 'send',
      UNDER_REVIEW: 'hourglass_empty',
      SHORTLISTED: 'star',
      INTERVIEW_SCHEDULED: 'event_available',
      OFFERED: 'workspace_premium',
      REJECTED: 'cancel'
    };
    return map[status] || 'info';
  }

  getStatusColor(status: string): string {
    const map: any = {
      APPLIED: 'primary',
      UNDER_REVIEW: 'accent',
      SHORTLISTED: 'warn',
      INTERVIEW_SCHEDULED: 'accent',
      OFFERED: 'primary',
      REJECTED: ''
    };
    return map[status] || 'primary';
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      APPLIED: 'status-applied',
      UNDER_REVIEW: 'status-review',
      SHORTLISTED: 'status-shortlisted',
      INTERVIEW_SCHEDULED: 'status-interview',
      OFFERED: 'status-offered',
      REJECTED: 'status-rejected'
    };
    return map[status] || 'status-applied';
  }

  formatStatus(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  hasResumeUrl(): boolean {
    return !!this.applicationStatus?.resumeUrl?.trim();
  }

  hasOfferLetterUrl(): boolean {
    return this.applicationStatus?.status === 'OFFERED' && !!this.applicationStatus.offerLetterUrl?.trim();
  }
}
