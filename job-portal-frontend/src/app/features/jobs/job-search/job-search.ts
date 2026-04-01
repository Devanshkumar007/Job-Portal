import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Job, JobSearchFilters } from '../../../core/services/auth';
import { JobService } from '../../../core/services/job';

@Component({
  selector: 'app-job-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule
  ],
  templateUrl: './job-search.html',
  styleUrl: './job-search.scss',
})
export class JobSearch implements OnInit {
  jobs: Job[] = [];
  filters: JobSearchFilters = {};
  totalJobs = 0;
  pageSize = 9;
  currentPage = 0;
  isLoading = false;
  hasSearched = false;
  sortBy = 'createdAt';
  direction: 'asc' | 'desc' = 'desc';

  constructor(
    private readonly jobService: JobService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.filters = {
        title: params.get('title')?.trim() || undefined,
        location: params.get('location')?.trim() || undefined,
        companyName: params.get('companyName')?.trim() || undefined,
        minSalary: this.toNumber(params.get('minSalary')),
        maxSalary: this.toNumber(params.get('maxSalary')),
        minExperience: this.toNumber(params.get('minExperience')),
        maxExperience: this.toNumber(params.get('maxExperience')),
      };
      this.currentPage = this.toNumber(params.get('page')) ?? 0;
      this.pageSize = this.toNumber(params.get('size')) ?? 9;
      this.sortBy = params.get('sortBy') || 'createdAt';
      this.direction = params.get('direction') === 'asc' ? 'asc' : 'desc';

      if (this.hasActiveFilters()) {
        this.fetchJobs();
      } else {
        this.jobs = [];
        this.totalJobs = 0;
        this.hasSearched = false;
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.updateQueryParams();
  }

  clearFilters(): void {
    this.filters = {};
    this.currentPage = 0;
    this.pageSize = 9;
    this.sortBy = 'createdAt';
    this.direction = 'desc';
    this.router.navigate(['/jobs/search']);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.updateQueryParams();
  }

  viewJob(id: number): void {
    this.router.navigate(['/jobs', id]);
  }

  formatSalary(salary: number): string {
    return `₹${(salary / 100000).toFixed(1)} LPA`;
  }

  private fetchJobs(): void {
    this.isLoading = true;
    this.hasSearched = true;

    this.jobService
      .searchJobs(this.sanitizedFilters(), this.currentPage, this.pageSize, this.sortBy, this.direction)
      .subscribe({
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

  private updateQueryParams(): void {
    this.router.navigate(['/jobs/search'], {
      queryParams: {
        ...this.sanitizedFilters(),
        page: this.currentPage,
        size: this.pageSize,
        sortBy: this.sortBy,
        direction: this.direction
      }
    });
  }

  private sanitizedFilters(): JobSearchFilters {
    return Object.fromEntries(
      Object.entries(this.filters).filter(([, value]) => value !== undefined && value !== null && value !== '')
    ) as JobSearchFilters;
  }

  private hasActiveFilters(): boolean {
    return Object.keys(this.sanitizedFilters()).length > 0;
  }

  private toNumber(value: string | null): number | undefined {
    if (value === null || value.trim() === '') {
      return undefined;
    }

    const parsed = Number(value);
    return Number.isNaN(parsed) ? undefined : parsed;
  }
}
