import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import {
  ApplicationService,
  RecruiterDashboardPayload,
  RecruiterDashboardRecentCandidate,
  RecruiterDashboardRecentJob
} from '../../../core/services/application';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './recruiter-dashboard.html',
  styleUrl: './recruiter-dashboard.scss'
})
export class RecruiterDashboard implements OnInit {
  recentJobs: RecruiterDashboardRecentJob[] = [];
  recentCandidates: RecruiterDashboardRecentCandidate[] = [];
  summary: RecruiterDashboardPayload['summary'] = {
    openRoles: 0,
    totalApplications: 0,
    shortlistedCount: 0,
    offersSent: 0
  };
  pipeline: RecruiterDashboardPayload['pipeline'] = {
    applicationsReceivedPercent: 0,
    interviewsScheduledPercent: 0,
    offersSentPercent: 0
  };
  isLoading = true;

  constructor(
    private applicationService: ApplicationService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadJobs();
  }

  loadJobs() {
    this.isLoading = true;
    this.applicationService.getRecruiterDashboard().subscribe({
      next: (dashboard) => {
        this.summary = dashboard.summary;
        this.recentJobs = dashboard.recentJobs ?? [];
        this.recentCandidates = dashboard.recentCandidates ?? [];
        this.pipeline = dashboard.pipeline;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to load the recruiter dashboard right now.',
            statusMessages: {
              403: 'You do not have access to the recruiter dashboard.',
              404: 'Recruiter dashboard data was not found.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  postNewJob() {
    this.router.navigate(['/recruiter/post-job']);
  }

  viewApplications(jobId: number) {
    this.router.navigate(['/recruiter/applications'], { queryParams: { jobId } });
  }

  openApplicationsHub() {
    this.router.navigate(['/recruiter/applications']);
  }

  openCandidateApplications(candidate: RecruiterDashboardRecentCandidate) {
    if (candidate.jobId) {
      this.viewApplications(candidate.jobId);
      return;
    }

    this.openApplicationsHub();
  }

  openCandidateProfile(email: string, event?: Event): void {
    event?.stopPropagation();

    if (!email?.trim()) {
      return;
    }

    this.router.navigate(['/users/email'], { queryParams: { email } });
  }

  viewJob(jobId: number) {
    this.router.navigate(['/jobs', jobId]);
  }

  reviewLatestApplications() {
    const latestJobId = this.recentJobs[0]?.jobId;
    if (latestJobId) {
      this.viewApplications(latestJobId);
    }
  }

  formatCandidateStatus(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  getCandidateStatusClass(status: string): string {
    const map: Record<string, string> = {
      APPLIED: 'candidate-status-applied',
      UNDER_REVIEW: 'candidate-status-review',
      SHORTLISTED: 'candidate-status-shortlisted',
      INTERVIEW_SCHEDULED: 'candidate-status-interview',
      OFFERED: 'candidate-status-offered',
      REJECTED: 'candidate-status-rejected'
    };

    return map[status] ?? 'candidate-status-applied';
  }

  getJobBadgeClass(status: string): string {
    return status === 'ACTIVE' ? 'job-badge-active' : 'job-badge-neutral';
  }

  get interviewScheduledCount(): number {
    return Math.round((this.summary.totalApplications * this.pipeline.interviewsScheduledPercent) / 100);
  }
}
