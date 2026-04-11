import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Application } from '../../../core/services/auth';
import { firstValueFrom } from 'rxjs';
import { getHttpErrorMessage } from '../../../core/utils/http-error';
import { Router } from '@angular/router';

type AdminApplicationRow = Application & {
  resolvedJobTitle?: string;
  resolvedApplicantEmail?: string;
};

@Component({
  selector: 'app-admin-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatPaginatorModule, MatSnackBarModule],
  templateUrl: './admin-applications.html',
  styleUrl: './admin-applications.scss'
})
export class AdminApplications implements OnInit {
  applications: AdminApplicationRow[] = [];
  totalApplications = 0;
  pageSize = 10;
  currentPage = 0;
  readonly pageSizeOptions = [10, 25, 50, 100];
  isLoading = true;

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit() { this.loadApplications(); }

  async loadApplications() {
    this.isLoading = true;
    try {
      const [pageResult, overviewResult] = await Promise.all([
        this.fetchApplicationsPage(this.currentPage, this.pageSize),
        this.fetchAllApplications()
      ]);

      const { items, total } = pageResult;
      this.totalApplications = total;

      try {
        this.applications = await this.resolveMissingFields(items);
      } catch {
        this.applications = items;
      }

      this.overviewSource = overviewResult.items;
    } catch {
      this.applications = [];
      this.totalApplications = 0;
      this.overviewSource = [];
    } finally {
      this.isLoading = false;
    }
  }

  deleteApplication(id: number) {
    if (!confirm('Delete this application?')) return;
    this.http.delete(`http://localhost:8080/api/admin/applications/${id}`).subscribe({
      next: () => { this.snackBar.open('Deleted!', 'Close', { duration: 3000 }); this.loadApplications(); },
      error: (err) => this.snackBar.open(
        getHttpErrorMessage(err, {
          defaultMessage: 'Unable to delete this application right now.',
          statusMessages: {
            403: 'You do not have permission to delete this application.',
            404: 'This application no longer exists.'
          }
        }),
        'Close',
        { duration: 3000 }
      )
    });
  }

  formatStatus(status: string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (char) => char.toUpperCase());
  }

  getStatusClass(status: string): string {
    const normalizedStatus = this.normalizeStatus(status);
    const map: Record<string, string> = {
      APPLIED: 'status-applied',
      UNDER_REVIEW: 'status-review',
      SHORTLISTED: 'status-shortlisted',
      INTERVIEW_SCHEDULED: 'status-interview',
      OFFERED: 'status-offered',
      REJECTED: 'status-rejected',
      HIRED: 'status-offered',
      ACCEPTED: 'status-offered',
      WITHDRAWN: 'status-rejected',
      CANCELLED: 'status-rejected'
    };

    return map[normalizedStatus] ?? 'status-default';
  }

  get overviewCards(): Array<{ label: string; value: number; tone: string }> {
    const statusCounts = this.overviewSource.reduce<Record<string, number>>((counts, application) => {
      const normalizedStatus = this.normalizeStatus(application.status);
      counts[normalizedStatus] = (counts[normalizedStatus] ?? 0) + 1;
      return counts;
    }, {});

    const orderedStatuses = Object.entries(statusCounts)
      .sort((a, b) => b[1] - a[1])
      .map(([status, value]) => ({
        label: this.formatStatus(status),
        value,
        tone: this.getStatusClass(status)
      }));

    return [
      { label: 'Total Applications', value: this.totalApplications || this.applications.length, tone: 'status-default' },
      ...orderedStatuses
    ];
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadApplications();
  }

  viewCandidateProfile(email?: string): void {
    if (!email?.trim()) {
      return;
    }

    this.router.navigate(['/users/email'], { queryParams: { email } });
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

  private overviewSource: AdminApplicationRow[] = [];

  private async fetchApplicationsPage(page: number, size: number): Promise<{ items: AdminApplicationRow[]; total: number }> {
    const response = await firstValueFrom(
      this.http.get<any>(`http://localhost:8080/api/admin/applications?page=${page}&size=${size}`)
    );

    const resolved = this.resolvePagedItems<AdminApplicationRow>(response);
    return {
      items: resolved.items,
      total: resolved.total
    };
  }

  private async fetchAllApplications(): Promise<{ items: AdminApplicationRow[]; total: number }> {
    const firstResponse = await firstValueFrom(
      this.http.get<any>('http://localhost:8080/api/admin/applications?page=0&size=100')
    );

    const firstPage = this.resolvePagedItems<AdminApplicationRow>(firstResponse);
    const totalPages = this.extractTotalPages(firstPage.payload, firstPage.items.length, firstPage.total);

    if (totalPages <= 1) {
      return { items: firstPage.items, total: firstPage.total };
    }

    const remainingResponses = await Promise.all(
      Array.from({ length: totalPages - 1 }, (_, index) =>
        firstValueFrom(this.http.get<any>(`http://localhost:8080/api/admin/applications?page=${index + 1}&size=100`))
      )
    );

    const allItems = [
      ...firstPage.items,
      ...remainingResponses.flatMap((response) => this.resolvePagedItems<AdminApplicationRow>(response).items)
    ];

    return {
      items: allItems,
      total: Math.max(firstPage.total, allItems.length)
    };
  }

  private resolvePagedItems<T>(response: any): { items: T[]; total: number; payload: any } {
    const payload = response?.data ?? response?.result ?? response;
    const items = this.extractItems<T>(payload);
    const total = this.extractTotal(payload, items.length);

    return { items, total, payload };
  }

  private extractItems<T>(payload: any): T[] {
    if (Array.isArray(payload)) {
      return payload;
    }

    if (Array.isArray(payload?.content)) {
      return payload.content;
    }

    if (Array.isArray(payload?.items)) {
      return payload.items;
    }

    if (Array.isArray(payload?.data)) {
      return payload.data;
    }

    return [];
  }

  private extractTotal(payload: any, fallback: number): number {
    const total = payload?.totalElements ?? payload?.total ?? payload?.count ?? payload?.length;
    return typeof total === 'number' ? total : fallback;
  }

  private extractTotalPages(payload: any, itemCount: number, total: number): number {
    const totalPages = payload?.totalPages;
    if (typeof totalPages === 'number' && totalPages > 0) {
      return totalPages;
    }

    const pageSize = payload?.size;
    if (typeof pageSize === 'number' && pageSize > 0) {
      return Math.max(1, Math.ceil(total / pageSize));
    }

    return itemCount > 0 && total > itemCount ? Math.ceil(total / itemCount) : 1;
  }

  private normalizeStatus(status: string | undefined | null): string {
    return (status ?? 'UNKNOWN').trim().toUpperCase().replace(/\s+/g, '_');
  }
}
