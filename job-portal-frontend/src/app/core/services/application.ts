import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application } from './auth';

@Injectable({ providedIn: 'root' })
export class ApplicationService {

  private apiUrl = 'http://localhost:8080/api/applications';

  constructor(private http: HttpClient) {}

  applyForJobWithForm(formData: FormData): Observable<Application> {
    return this.http.post<Application>(`${this.apiUrl}/apply`, formData);
  }

  getMyApplications(userId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/user/${userId}`);
  }

  getApplicationsByJob(jobId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/job/${jobId}`);
  }

  updateApplicationStatus(applicationId: number, status: string, company: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/status`, { applicationId, status, company });
  }
}