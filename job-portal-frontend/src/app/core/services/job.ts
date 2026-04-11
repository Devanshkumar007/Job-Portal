import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
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

  getJobsByRecruiter(recruiterId: number, page: number = 0, size: number = 100): Observable<PagedResponse<Job>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', 'createdAt')
      .set('direction', 'desc')
      .set('recruiterId', recruiterId);

    return this.http.get<PagedResponse<Job> | Job[]>(this.apiUrl, { params }).pipe(
      map((response) => {
        if (Array.isArray(response)) {
          return {
            content: response,
            totalElements: response.length,
            totalPages: 1,
            size: response.length,
            number: 0
          };
        }
        return response;
      })
    );
  }

  updateJob(id: number, job: JobRequest, recruiterId: number): Observable<Job> {
    const headers = new HttpHeaders({
      'X-User-Id': String(recruiterId)
    });

    return this.http.put<Job>(`${this.apiUrl}/${id}`, job, { headers });
  }

  deleteJob(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteJobAsAdmin(id: number): Observable<void> {
    return this.http.delete<void>(`http://localhost:8080/api/admin/jobs/${id}`);
  }
}
