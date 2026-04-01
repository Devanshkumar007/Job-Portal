import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Application } from '../../../core/services/auth';
import { firstValueFrom } from 'rxjs';

type AdminApplicationRow = Application & {
  resolvedJobTitle?: string;
  resolvedApplicantEmail?: string;
};

@Component({
  selector: 'app-admin-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  templateUrl: './admin-applications.html',
  styleUrl: './admin-applications.scss'
})
export class AdminApplications implements OnInit {
  applications: AdminApplicationRow[] = [];
  isLoading = true;
  displayedColumns = ['jobTitle', 'user', 'resume', 'status', 'appliedAt', 'actions'];

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {}

  ngOnInit() { this.loadApplications(); }

  loadApplications() {
    this.isLoading = true;
    this.http.get<any>('http://localhost:8080/api/admin/applications').subscribe({
      next: (res) => {
        const applications = (res.content ?? res) as AdminApplicationRow[];
        this.resolveMissingFields(applications)
          .then((resolved) => {
            this.applications = resolved;
            this.isLoading = false;
          })
          .catch(() => {
            this.applications = applications;
            this.isLoading = false;
          });
      },
      error: () => { this.isLoading = false; }
    });
  }

  deleteApplication(id: number) {
    if (!confirm('Delete this application?')) return;
    this.http.delete(`http://localhost:8080/api/admin/applications/${id}`).subscribe({
      next: () => { this.snackBar.open('Deleted!', 'Close', { duration: 3000 }); this.loadApplications(); },
      error: () => this.snackBar.open('Failed.', 'Close', { duration: 3000 })
    });
  }

  getStatusColor(status: string): string {
    const map: any = { APPLIED: 'primary', UNDER_REVIEW: 'accent', SHORTLISTED: 'warn', REJECTED: '' };
    return map[status] || 'primary';
  }

  private async resolveMissingFields(applications: AdminApplicationRow[]): Promise<AdminApplicationRow[]> {
    return Promise.all(
      applications.map(async (application) => {
        const resolved: AdminApplicationRow = { ...application };

        if (!resolved.jobTitle?.trim() && resolved.jobId !== undefined && resolved.jobId !== null) {
          try {
            const job = await firstValueFrom(this.http.get<any>(`http://localhost:8080/api/admin/jobs/${resolved.jobId}`));
            resolved.resolvedJobTitle = job?.title;
          } catch {
            resolved.resolvedJobTitle = undefined;
          }
        }

        if (!resolved.applicantEmail?.trim() && resolved.userId !== undefined && resolved.userId !== null) {
          try {
            const user = await firstValueFrom(this.http.get<any>(`http://localhost:8080/api/admin/users/${resolved.userId}`));
            resolved.resolvedApplicantEmail = user?.email;
          } catch {
            resolved.resolvedApplicantEmail = undefined;
          }
        }

        return resolved;
      })
    );
  }
}
