import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../../core/services/job';
import { AuthService, Job } from '../../../core/services/auth';

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatSnackBarModule
  ],
  templateUrl: './recruiter-dashboard.html',
  styleUrl: './recruiter-dashboard.scss'
})
export class RecruiterDashboard implements OnInit {
  jobs: Job[] = [];
  isLoading = true;
  displayedColumns = ['title', 'company', 'location', 'salary', 'actions'];

  constructor(
    private jobService: JobService,
    public authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadJobs();
  }

  loadJobs() {
    this.isLoading = true;
    this.jobService.getAllJobs(0, 100).subscribe({
      next: (response) => {
        const user = this.authService.getCurrentUser();
        this.jobs = response.content.filter(j => j.recruiterId === user?.id);
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  postNewJob() {
    this.router.navigate(['/recruiter/post-job']);
  }

  viewApplications(jobId: number) {
    this.router.navigate(['/recruiter/applications'], { queryParams: { jobId } });
  }

  deleteJob(jobId: number) {
    if (!confirm('Are you sure you want to delete this job?')) return;
    this.jobService.deleteJob(jobId).subscribe({
      next: () => {
        this.snackBar.open('Job deleted!', 'Close', { duration: 3000 });
        this.loadJobs();
      },
      error: () => this.snackBar.open('Failed to delete job.', 'Close', { duration: 3000 })
    });
  }

  formatSalary(salary: number): string {
    return `₹${(salary / 100000).toFixed(1)} LPA`;
  }
}