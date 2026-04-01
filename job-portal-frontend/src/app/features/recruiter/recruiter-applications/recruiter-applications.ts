import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../core/services/application';
import { Application } from '../../../core/services/auth';

@Component({
  selector: 'app-recruiter-applications',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatSelectModule, MatFormFieldModule, MatSnackBarModule],
  templateUrl: './recruiter-applications.html',
  styleUrl: './recruiter-applications.scss'
})
export class RecruiterApplications implements OnInit {
  applications: Application[] = [];
  isLoading = true;
  jobId: number | null = null;
  displayedColumns = ['applicant', 'jobTitle', 'company', 'status', 'appliedAt', 'actions'];
  statusOptions = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'REJECTED'];

  constructor(
    private applicationService: ApplicationService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.jobId = params['jobId'] ? Number(params['jobId']) : null;
      this.loadApplications();
    });
  }

  loadApplications() {
    if (!this.jobId) return;
    this.isLoading = true;
    this.applicationService.getApplicationsByJob(this.jobId).subscribe({
      next: (apps) => { this.applications = apps; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  updateStatus(app: Application, status: string) {
    this.applicationService.updateApplicationStatus(app.id, status, app.company).subscribe({
      next: () => {
        app.status = status as any;
        this.snackBar.open(`Status updated to ${status}`, 'Close', { duration: 3000 });
      },
      error: () => this.snackBar.open('Failed to update status.', 'Close', { duration: 3000 })
    });
  }

  getStatusColor(status: string): string {
    const map: any = { APPLIED: 'primary', UNDER_REVIEW: 'accent', SHORTLISTED: 'warn', REJECTED: '' };
    return map[status] || 'primary';
  }
}