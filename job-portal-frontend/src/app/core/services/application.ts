import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application, AuthService, PagedResponse } from './auth';

export interface RecruiterDashboardSummary {
  openRoles: number;
  totalApplications: number;
  shortlistedCount: number;
  offersSent: number;
}

export interface RecruiterDashboardRecentJob {
  jobId: number;
  title: string;
  companyName: string;
  status: string;
}

export interface RecruiterDashboardRecentCandidate {
  applicationId: number;
  jobId: number;
  applicantName: string;
  applicantEmail: string;
  jobTitle: string;
  status: string;
}

export interface RecruiterDashboardPipeline {
  applicationsReceivedPercent: number;
  interviewsScheduledPercent: number;
  offersSentPercent: number;
}

export interface RecruiterDashboardPayload {
  summary: RecruiterDashboardSummary;
  recentJobs: RecruiterDashboardRecentJob[];
  recentCandidates: RecruiterDashboardRecentCandidate[];
  pipeline: RecruiterDashboardPipeline;
}

export interface ApplicationStatusUpdateRequest {
  applicationId: number;
  company?: string;
  status: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED' | 'OFFERED' | 'REJECTED';
  interviewLink?: string;
  interviewDate?: string;
  interviewTime?: string;
  timeZone?: string;
}

@Injectable({ providedIn: 'root' })
export class ApplicationService {

  private apiUrl = 'http://localhost:8080/api/applications';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  applyForJobWithForm(formData: FormData): Observable<Application> {
    return this.http.post<Application>(`${this.apiUrl}/apply`, formData);
  }

  getMyApplications(userId: number, page: number = 0, size: number = 10): Observable<PagedResponse<Application>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PagedResponse<Application>>(`${this.apiUrl}/user/${userId}`, { params });
  }

  getMyApplicationsByStatus(
    status: Application['status'],
    page: number = 0,
    size: number = 10
  ): Observable<PagedResponse<Application>> {
    const currentUser = this.authService.getCurrentUser();
    const params = new HttpParams()
      .set('status', status)
      .set('page', page)
      .set('size', size);
    const headers = new HttpHeaders({
      'X-User-Id': String(currentUser?.id ?? ''),
      'X-User-Role': String(currentUser?.role ?? '')
    });

    return this.http.get<PagedResponse<Application>>(`${this.apiUrl}/user/by-status`, { params, headers });
  }

  getApplicationsByJob(jobId: number, page: number = 0, size: number = 10): Observable<PagedResponse<Application>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PagedResponse<Application>>(`${this.apiUrl}/job/${jobId}`, { params });
  }

  getRecruiterDashboard(): Observable<RecruiterDashboardPayload> {
    return this.http.get<RecruiterDashboardPayload>('http://localhost:8080/api/recruiter/dashboard');
  }

  updateApplicationStatus(request: ApplicationStatusUpdateRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/status`, request);
  }

  updateApplicationStatusToOffered(applicationId: number, company: string, file: File): Observable<Application> {
    const formData = new FormData();
    formData.append('applicationId', String(applicationId));
    formData.append('company', company ?? '');
    formData.append('file', file);

    return this.http.put<Application>(`${this.apiUrl}/status/offered`, formData);
  }
}
