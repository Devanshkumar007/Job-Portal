import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { JobService } from '../../../core/services/job';
import { AuthService, Job, JobSearchFilters } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatChipsModule,
    MatSelectModule,
    MatPaginatorModule,
    MatSnackBarModule
  ],
  templateUrl: './job-list.html',
  styleUrl: './job-list.scss'
})
export class JobList implements OnInit {
  jobs: Job[] = [];
  filters: JobSearchFilters = {};
  totalJobs = 0;
  pageSize = 9;
  currentPage = 0;
  isLoading = true;
  searchKeyword = '';
  hasActiveFilters = false;
  readonly jobTypeOptions = [
    { value: 'FULL_TIME', label: 'Full Time' },
    { value: 'PART_TIME', label: 'Part Time' },
    { value: 'INTERNSHIP', label: 'Internship' }
  ] as const;

  constructor(
    private readonly jobService: JobService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    public readonly authService: AuthService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      this.filters = {
        title: params.get('title')?.trim() || undefined,
        location: params.get('location')?.trim() || undefined,
        companyName: params.get('companyName')?.trim() || undefined,
        jobType: (params.get('jobType')?.trim() as JobSearchFilters['jobType']) || undefined,
        minSalary: this.toNumber(params.get('minSalary')),
        maxSalary: this.toNumber(params.get('maxSalary')),
        minExperience: this.toNumber(params.get('minExperience')),
        maxExperience: this.toNumber(params.get('maxExperience')),
      };
      this.searchKeyword = this.filters.title ?? '';
      this.currentPage = this.toNumber(params.get('page')) ?? 0;
      this.pageSize = this.toNumber(params.get('size')) ?? 9;
      this.hasActiveFilters = Object.keys(this.sanitizedFilters()).length > 0;
      this.loadJobs();
    });
  }

  loadJobs() {
    this.isLoading = true;
    const request$ = this.hasActiveFilters
      ? this.jobService.searchJobs(this.sanitizedFilters(), this.currentPage, this.pageSize)
      : this.jobService.getAllJobs(this.currentPage, this.pageSize);

    request$.subscribe({
      next: (response) => {
        this.jobs = response.content;
        this.totalJobs = response.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.jobs = [];
        this.totalJobs = 0;
        this.isLoading = false;
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateQueryParams();
  }

  viewJob(id: number) {
    this.router.navigate(['/jobs', id]);
  }

  searchJobs() {
    this.filters.title = this.searchKeyword.trim() || undefined;
    this.currentPage = 0;
    this.updateQueryParams();
  }

  applyFilters() {
    this.searchKeyword = this.filters.title ?? '';
    this.currentPage = 0;
    this.updateQueryParams();
  }

  clearFilters() {
    this.filters = {};
    this.searchKeyword = '';
    this.currentPage = 0;
    this.pageSize = 9;
    this.router.navigate(['/jobs']);
  }

  canDeleteJob(job: Job): boolean {
    const user = this.authService.getCurrentUser();
    if (!user) return false;
    if (this.authService.isAdmin()) return true;
    if (this.authService.isRecruiter()) return job.recruiterId === user.id;
    return false;
  }

  deleteJob(event: Event, jobId: number) {
    event.stopPropagation();
    const confirmed = confirm('Are you sure you want to delete this job? This action cannot be undone.');
    if (!confirmed) return;

    const deleteRequest$ = this.authService.isAdmin()
      ? this.jobService.deleteJobAsAdmin(jobId)
      : this.jobService.deleteJob(jobId);

    deleteRequest$.subscribe({
      next: () => {
        this.snackBar.open('Job deleted successfully!', 'Close', { duration: 3000 });
        this.loadJobs();
      },
      error: (err: any) => {
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

  formatSalary(job: Job): string {
    if (job.jobType === 'INTERNSHIP') {
      return `₹${Math.round(job.salary).toLocaleString('en-IN')}/month`;
    }
    return `₹${(job.salary / 100000).toFixed(1)} LPA`;
  }

  formatJobType(job: Job): string {
    if (!job.jobType) return '';
    if (job.jobType === 'FULL_TIME') return 'Full Time';
    if (job.jobType === 'PART_TIME') return 'Part Time';
    if (job.internshipDurationMonths) return `Internship • ${job.internshipDurationMonths} mo`;
    return 'Internship';
  }

  formatJobTypeLabel(jobType?: JobSearchFilters['jobType']): string {
    if (!jobType) return '';
    return this.jobTypeOptions.find((option) => option.value === jobType)?.label ?? jobType;
  }

  getViewButtonLabel(): string {
    return this.authService.isRecruiter() ? 'View' : 'View & Apply';
  }

  private updateQueryParams() {
    this.router.navigate(['/jobs'], {
      queryParams: {
        ...this.sanitizedFilters(),
        page: this.currentPage,
        size: this.pageSize
      }
    });
  }

  private sanitizedFilters(): JobSearchFilters {
    return Object.fromEntries(
      Object.entries(this.filters).filter(([, value]) => value !== undefined && value !== null && value !== '')
    ) as JobSearchFilters;
  }

  private toNumber(value: string | null): number | undefined {
    if (value === null || value.trim() === '') return undefined;
    const parsed = Number(value);
    return Number.isNaN(parsed) ? undefined : parsed;
  }
}
