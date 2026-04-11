import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ApplicationService, ApplicationStatusUpdateRequest } from '../../../core/services/application';
import { Application, AuthService, Job } from '../../../core/services/auth';
import { JobService } from '../../../core/services/job';
import { StatusChangeDialog, StatusChangeDialogResult } from './status-change-dialog/status-change-dialog';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-recruiter-applications',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatPaginatorModule, MatSnackBarModule],
  templateUrl: './recruiter-applications.html',
  styleUrl: './recruiter-applications.scss'
})
export class RecruiterApplications implements OnInit {
  applications: Application[] = [];
  overviewApplications: Application[] = [];
  recruiterJobs: Job[] = [];
  isLoading = true;
  isLoadingJobs = true;
  updatingApplicationId: number | null = null;
  jobId: number | null = null;
  totalApplications = 0;
  pageSize = 10;
  currentPage = 0;
  displayedColumns = ['applicant', 'jobTitle', 'company', 'status', 'actions', 'appliedAt', 'resume'];
  readonly allowedTransitions: Record<Application['status'], Application['status'][]> = {
    APPLIED: ['UNDER_REVIEW', 'SHORTLISTED', 'REJECTED'],
    UNDER_REVIEW: ['SHORTLISTED', 'REJECTED'],
    SHORTLISTED: ['INTERVIEW_SCHEDULED', 'REJECTED'],
    INTERVIEW_SCHEDULED: ['OFFERED', 'REJECTED'],
    OFFERED: [],
    REJECTED: []
  };

  constructor(
    private applicationService: ApplicationService,
    private jobService: JobService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.jobId = params['jobId'] ? Number(params['jobId']) : null;
      this.currentPage = params['page'] ? Number(params['page']) : 0;
      this.pageSize = params['size'] ? Number(params['size']) : 10;
      this.loadRecruiterJobs();
    });
  }

  loadRecruiterJobs() {
    const recruiterId = this.authService.getCurrentUser()?.id;
    if (!recruiterId) {
      this.recruiterJobs = [];
      this.isLoadingJobs = false;
      this.applications = [];
      this.isLoading = false;
      return;
    }

    this.isLoadingJobs = true;
    this.jobService.getJobsByRecruiter(recruiterId, 0, 100).subscribe({
      next: (response) => {
        this.recruiterJobs = (response?.content ?? [])
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

        this.isLoadingJobs = false;

        if (!this.recruiterJobs.length) {
          this.jobId = null;
          this.applications = [];
          this.overviewApplications = [];
          this.totalApplications = 0;
          this.isLoading = false;
          return;
        }

        const hasSelectedJob = this.jobId !== null && this.recruiterJobs.some(job => job.id === this.jobId);
        const nextJobId: number = hasSelectedJob && this.jobId !== null ? this.jobId : this.recruiterJobs[0].id;

        if (this.jobId !== nextJobId) {
          this.selectJob(nextJobId);
          return;
        }

        this.loadApplications();
      },
      error: () => {
        this.recruiterJobs = [];
        this.overviewApplications = [];
        this.isLoadingJobs = false;
        this.isLoading = false;
      }
    });
  }

  loadApplications() {
    if (!this.jobId) {
      this.applications = [];
      this.overviewApplications = [];
      this.totalApplications = 0;
      this.isLoading = false;
      return;
    }
    this.isLoading = true;
    this.applicationService.getApplicationsByJob(this.jobId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.applications = response.content;
        this.totalApplications = response.totalElements;
        this.isLoading = false;
        this.loadOverviewApplications(this.jobId!);
      },
      error: () => { this.isLoading = false; }
    });
  }

  selectJob(jobId: number) {
    this.currentPage = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { jobId, page: 0, size: this.pageSize },
      queryParamsHandling: 'merge'
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: this.currentPage, size: this.pageSize },
      queryParamsHandling: 'merge'
    });
  }

  get selectedJob(): Job | undefined {
    return this.recruiterJobs.find(job => job.id === this.jobId);
  }

  get overviewCards(): Array<{ label: string; value: number; tone: string }> {
    const statusCounts = this.overviewApplications.reduce<Record<string, number>>((counts, application) => {
      const normalizedStatus = this.normalizeStatus(application.status);
      counts[normalizedStatus] = (counts[normalizedStatus] ?? 0) + 1;
      return counts;
    }, {});

    const statusCards = Object.entries(statusCounts)
      .sort((a, b) => b[1] - a[1])
      .map(([status, value]) => ({
        label: this.formatStatus(status),
        value,
        tone: this.getStatusClass(status as Application['status'])
      }));

    return [
      {
        label: 'Total Applications',
        value: this.totalApplications || this.overviewApplications.length,
        tone: 'status-badge-default'
      },
      ...statusCards
    ];
  }

  updateStatus(app: Application, status: Application['status']) {
    if (status === app.status) {
      return;
    }

    if (!this.allowedTransitions[app.status].includes(status)) {
      this.snackBar.open(
        `Invalid status transition from ${this.formatStatus(app.status)} to ${this.formatStatus(status)}.`,
        'Close',
        { duration: 3500 }
      );
      return;
    }

    const dialogRef = this.dialog.open(StatusChangeDialog, {
      width: '560px',
      maxWidth: '95vw',
      data: { application: app, nextStatus: status }
    });

    dialogRef.afterClosed().subscribe((result?: StatusChangeDialogResult) => {
      if (!result?.confirmed) {
        return;
      }

      this.updatingApplicationId = app.id;

      if (result.status === 'OFFERED' && result.offerLetterFile) {
        this.applicationService.updateApplicationStatusToOffered(app.id, app.company, result.offerLetterFile).subscribe({
          next: (updatedApp) => this.handleStatusUpdateSuccess(app, updatedApp, 'Offer sent successfully.'),
          error: (err) => this.handleStatusUpdateError(err)
        });
        return;
      }

      const request: ApplicationStatusUpdateRequest = {
        applicationId: app.id,
        company: app.company,
        status: result.status,
        interviewLink: result.interviewLink,
        interviewDate: result.interviewDate,
        interviewTime: result.interviewTime,
        timeZone: result.timeZone
      };

      this.applicationService.updateApplicationStatus(request).subscribe({
        next: (updatedApp) => this.handleStatusUpdateSuccess(app, updatedApp, `Status updated to ${this.formatStatus(result.status)}.`),
        error: (err) => this.handleStatusUpdateError(err)
      });
    });
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

  formatStatus(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  openApplicantProfile(email: string, event?: Event): void {
    event?.stopPropagation();

    if (!email?.trim()) {
      return;
    }

    this.router.navigate(['/users/email'], { queryParams: { email } });
  }

  getAvailableActions(app: Application): Application['status'][] {
    return this.allowedTransitions[app.status];
  }

  isStatusLocked(app: Application): boolean {
    return this.updatingApplicationId === app.id || this.allowedTransitions[app.status].length === 0;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      APPLIED: 'status-badge-applied',
      UNDER_REVIEW: 'status-badge-review',
      SHORTLISTED: 'status-badge-shortlisted',
      INTERVIEW_SCHEDULED: 'status-badge-interview',
      OFFERED: 'status-badge-offered',
      REJECTED: 'status-badge-rejected'
    };

    return map[this.normalizeStatus(status)] ?? 'status-badge-default';
  }

  getStatusIcon(status: Application['status']): string {
    const map: Record<Application['status'], string> = {
      APPLIED: 'send',
      UNDER_REVIEW: 'manage_search',
      SHORTLISTED: 'star',
      INTERVIEW_SCHEDULED: 'event',
      OFFERED: 'workspace_premium',
      REJECTED: 'cancel'
    };

    return map[status];
  }

  getActionLabel(status: Application['status']): string {
    const map: Record<Application['status'], string> = {
      APPLIED: 'Mark Applied',
      UNDER_REVIEW: 'Move to Review',
      SHORTLISTED: 'Shortlist',
      INTERVIEW_SCHEDULED: 'Schedule Interview',
      OFFERED: 'Send Offer',
      REJECTED: 'Reject'
    };

    return map[status];
  }

  getActionIcon(status: Application['status']): string {
    const map: Record<Application['status'], string> = {
      APPLIED: 'send',
      UNDER_REVIEW: 'visibility',
      SHORTLISTED: 'star',
      INTERVIEW_SCHEDULED: 'event_available',
      OFFERED: 'workspace_premium',
      REJECTED: 'block'
    };

    return map[status];
  }

  getActionButtonClass(status: Application['status']): string {
    const map: Record<Application['status'], string> = {
      APPLIED: 'action-btn-applied',
      UNDER_REVIEW: 'action-btn-review',
      SHORTLISTED: 'action-btn-shortlisted',
      INTERVIEW_SCHEDULED: 'action-btn-interview',
      OFFERED: 'action-btn-offered',
      REJECTED: 'action-btn-rejected'
    };

    return map[status];
  }

  private handleStatusUpdateSuccess(currentApp: Application, updatedApp: Application, message: string): void {
    Object.assign(currentApp, updatedApp);
    this.updatingApplicationId = null;
    this.snackBar.open(message, 'Close', { duration: 3200 });
    if (this.jobId) {
      this.loadOverviewApplications(this.jobId);
    }
  }

  private handleStatusUpdateError(err: any): void {
    this.updatingApplicationId = null;
    this.snackBar.open(
      getHttpErrorMessage(err, {
        defaultMessage: 'Unable to update the application status right now.',
        statusMessages: {
          400: 'The status update request is invalid.',
          403: 'You do not have permission to update this application.',
          404: 'This application could not be found.'
        }
      }),
      'Close',
      { duration: 3500 }
    );
  }

  private async loadOverviewApplications(jobId: number): Promise<void> {
    try {
      const firstPage = await firstValueFrom(this.applicationService.getApplicationsByJob(jobId, 0, 100));
      const totalPages = Math.max(firstPage.totalPages ?? 1, 1);

      if (totalPages <= 1) {
        if (this.jobId !== jobId) return;
        this.overviewApplications = firstPage.content ?? [];
        return;
      }

      const remainingPages = await Promise.all(
        Array.from({ length: totalPages - 1 }, (_, index) =>
          firstValueFrom(this.applicationService.getApplicationsByJob(jobId, index + 1, 100))
        )
      );

      if (this.jobId !== jobId) return;
      this.overviewApplications = [
        ...(firstPage.content ?? []),
        ...remainingPages.flatMap((page) => page.content ?? [])
      ];
    } catch {
      this.overviewApplications = [...this.applications];
    }
  }

  private normalizeStatus(status: string | undefined | null): string {
    return (status ?? 'UNKNOWN').trim().toUpperCase().replace(/\s+/g, '_');
  }
}
