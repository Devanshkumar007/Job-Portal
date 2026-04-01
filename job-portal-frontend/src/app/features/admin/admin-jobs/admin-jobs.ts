import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Job } from '../../../core/services/auth';

@Component({
  selector: 'app-admin-jobs',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  templateUrl: './admin-jobs.html',
  styleUrl: './admin-jobs.scss'
})
export class AdminJobs implements OnInit {
  jobs: Job[] = [];
  isLoading = true;
  displayedColumns = ['title', 'company', 'location', 'salary', 'experience', 'actions'];

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {}

  ngOnInit() { this.loadJobs(); }

  loadJobs() {
    this.isLoading = true;
    this.http.get<any>('http://localhost:8080/api/admin/jobs?page=0&size=100&sortBy=createdAt&direction=desc').subscribe({
      next: (res) => { this.jobs = res.content ?? res; this.isLoading = false; },
      error: () => {
        this.isLoading = false;
        this.snackBar.open('Failed to load jobs.', 'Close', { duration: 3000 });
      }
    });
  }

  deleteJob(id: number) {
    if (!confirm('Delete this job?')) return;
    this.http.delete(`http://localhost:8080/api/admin/jobs/${id}`).subscribe({
      next: () => { this.snackBar.open('Job deleted!', 'Close', { duration: 3000 }); this.loadJobs(); },
      error: () => this.snackBar.open('Failed to delete.', 'Close', { duration: 3000 })
    });
  }

  formatSalary(salary: number): string {
    return `₹${(salary / 100000).toFixed(1)} LPA`;
  }
}
