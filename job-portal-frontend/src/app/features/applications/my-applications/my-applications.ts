import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { ApplicationService } from '../../../core/services/application';
import { AuthService, Application } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatPaginatorModule, MatSnackBarModule],
  templateUrl: './my-applications.html',
  styleUrl: './my-applications.scss'
})
export class MyApplications implements OnInit {
  applications: Application[] = [];
  isLoading = true;
  totalApplications = 0;
  pageSize = 10;
  currentPage = 0;
  selectedStatus: 'ALL' | Application['status'] = 'ALL';
  readonly statusOptions: Array<{ value: 'ALL' | Application['status']; label: string }> = [
    { value: 'ALL', label: 'All Applications' },
    { value: 'APPLIED', label: 'Applied' },
    { value: 'UNDER_REVIEW', label: 'Under Review' },
    { value: 'SHORTLISTED', label: 'Shortlisted' },
    { value: 'INTERVIEW_SCHEDULED', label: 'Interview Scheduled' },
    { value: 'OFFERED', label: 'Offered' },
    { value: 'REJECTED', label: 'Rejected' }
  ];

  constructor(
    private applicationService: ApplicationService,
    public authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.isLoading = true;
    const request$ = this.selectedStatus === 'ALL'
      ? this.applicationService.getMyApplications(user.id, this.currentPage, this.pageSize)
      : this.applicationService.getMyApplicationsByStatus(this.selectedStatus, this.currentPage, this.pageSize);

    request$.subscribe({
      next: (response) => {
        this.applications = response.content;
        this.totalApplications = response.totalElements;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to load your applications right now.',
            statusMessages: {
              404: 'No applications were found for your account.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadApplications();
  }

  onStatusChange(status: 'ALL' | Application['status']): void {
    if (this.selectedStatus === status) {
      return;
    }

    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadApplications();
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

  formatStatus(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  get historyTitle(): string {
    return this.selectedStatus === 'ALL'
      ? 'Application history'
      : `${this.formatStatus(this.selectedStatus)} applications`;
  }

  get historyDescription(): string {
    if (this.selectedStatus === 'ALL') {
      return `${this.totalApplications} total applications. Showing your latest submissions and outcomes.`;
    }

    return `${this.totalApplications} ${this.formatStatus(this.selectedStatus).toLowerCase()} applications found.`;
  }

  get emptyStateTitle(): string {
    return this.selectedStatus === 'ALL' ? 'No applications yet' : `No ${this.formatStatus(this.selectedStatus).toLowerCase()} applications`;
  }

  get emptyStateDescription(): string {
    return this.selectedStatus === 'ALL'
      ? 'Start applying for jobs to track them here.'
      : 'Try switching to another status or keep exploring new roles.';
  }

  get totalReviewed(): number {
    return this.applications.filter((app) => ['UNDER_REVIEW', 'INTERVIEW_SCHEDULED'].includes(app.status)).length;
  }

  get totalShortlisted(): number {
    return this.applications.filter((app) => app.status === 'SHORTLISTED').length;
  }

  get totalOffered(): number {
    return this.applications.filter((app) => app.status === 'OFFERED').length;
  }

  getStatusClass(status: Application['status']): string {
    const map: Record<Application['status'], string> = {
      APPLIED: 'status-applied',
      UNDER_REVIEW: 'status-review',
      SHORTLISTED: 'status-shortlisted',
      INTERVIEW_SCHEDULED: 'status-interview',
      OFFERED: 'status-offered',
      REJECTED: 'status-rejected'
    };

    return map[status];
  }

  isStageActive(currentStatus: Application['status'], stage: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED' | 'OFFERED'): boolean {
    const order: Record<'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED' | 'OFFERED', number> = {
      APPLIED: 1,
      UNDER_REVIEW: 2,
      SHORTLISTED: 3,
      INTERVIEW_SCHEDULED: 4,
      OFFERED: 5
    };

    if (currentStatus === 'REJECTED') {
      return stage === 'APPLIED';
    }

    return order[currentStatus as keyof typeof order] >= order[stage];
  }

  hasResume(application: Application): boolean {
    return !!application.resumeUrl?.trim();
  }

  hasOfferLetter(application: Application): boolean {
    return application.status === 'OFFERED' && !!application.offerLetterUrl?.trim();
  }
}
