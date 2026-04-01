import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
  createdAt?: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
}

export interface AuthResponse {
  id: number;
  token: string;
  name: string;
  email: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: string;
  role: string;
}

export interface Job {
  id: number;
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
  recruiterId: number;
  createdAt: string;
}

export interface JobRequest {
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
  recruiterEmail: string;
}

export interface Application {
  id: number;
  userId: number;
  jobId: number;
  jobTitle: string;
  applicantEmail: string;
  resumeUrl: string;
  company: string;
  status: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'REJECTED';
  appliedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface JobSearchFilters {
  title?: string;
  location?: string;
  companyName?: string;
  minSalary?: number;
  maxSalary?: number;
  minExperience?: number;
  maxExperience?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response: AuthResponse) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify({
          id: response.id,
          name: response.name,
          email: response.email,
          role: response.role
        }));
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getRole(): string | null {
    return this.getCurrentUser()?.role ?? null;
  }

  isAdmin(): boolean { return this.getRole() === 'ADMIN'; }
  isRecruiter(): boolean { return this.getRole() === 'RECRUITER'; }
  isJobSeeker(): boolean { return this.getRole() === 'JOB_SEEKER'; }
}
