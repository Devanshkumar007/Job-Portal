import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Job } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-admin-jobs',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatPaginatorModule, MatSnackBarModule],
  templateUrl: './admin-jobs.html',
  styleUrl: './admin-jobs.scss'
})
export class AdminJobs implements OnInit {
  jobs: Job[] = [];
  isLoading = true;
  totalJobs = 0;
  pageSize = 10;
  currentPage = 0;
  displayedColumns = ['title', 'company', 'location', 'salary', 'experience', 'actions'];

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit() { this.loadJobs(); }

  loadJobs() {
    this.isLoading = true;
    this.http.get<any>(`http://localhost:8080/api/admin/jobs?page=${this.currentPage}&size=${this.pageSize}&sortBy=createdAt&direction=desc`).subscribe({
      next: (res) => {
        this.jobs = res.content ?? res;
        this.totalJobs = res.totalElements ?? this.jobs.length;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to load jobs right now.',
            statusMessages: {
              403: 'You do not have permission to view jobs.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  viewJob(id: number) {
    this.router.navigate(['/jobs', id]);
  }

  deleteJob(event: Event, id: number) {
    event.stopPropagation();
    if (!confirm('Delete this job?')) return;
    this.http.delete(`http://localhost:8080/api/admin/jobs/${id}`).subscribe({
      next: () => { this.snackBar.open('Job deleted!', 'Close', { duration: 3000 }); this.loadJobs(); },
      error: (err) => this.snackBar.open(
        getHttpErrorMessage(err, {
          defaultMessage: 'Unable to delete this job right now.',
          statusMessages: {
            403: 'You do not have permission to delete this job.',
            404: 'This job no longer exists.'
          }
        }),
        'Close',
        { duration: 3000 }
      )
    });
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadJobs();
  }

  formatSalary(job: Job): string {
    if (job.jobType === 'INTERNSHIP') {
      return `₹${Math.round(job.salary).toLocaleString('en-IN')}/month`;
    }
    return `₹${(job.salary / 100000).toFixed(1)} LPA`;
  }
}
