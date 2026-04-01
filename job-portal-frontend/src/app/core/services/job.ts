import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Job, JobRequest, JobSearchFilters, PagedResponse } from './auth';

@Injectable({ providedIn: 'root' })
export class JobService {

  private apiUrl = 'http://localhost:8080/api/jobs';

  constructor(private http: HttpClient) {}

  getAllJobs(page: number = 0, size: number = 10): Observable<PagedResponse<Job>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createdAt')
      .set('direction', 'desc');
    return this.http.get<PagedResponse<Job>>(this.apiUrl, { params });
  }

  getJobById(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/${id}`);
  }

  searchJobs(
    filters: JobSearchFilters,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'createdAt',
    direction: 'asc' | 'desc' = 'desc'
  ): Observable<PagedResponse<Job>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    return this.http.post<PagedResponse<Job>>(`${this.apiUrl}/search`, filters, { params });
  }

  createJob(job: JobRequest): Observable<Job> {
    return this.http.post<Job>(this.apiUrl, job);
  }

  updateJob(id: number, job: JobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}`, job);
  }

  deleteJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
