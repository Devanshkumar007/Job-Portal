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

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface Job {
  id: number;
  title: string;
  companyName: string;
  location: string;
  salary: number;
  experience: number;
  description: string;
  jobType?: 'FULL_TIME' | 'PART_TIME' | 'INTERNSHIP';
  internshipDurationMonths?: number | null;
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
  jobType: 'FULL_TIME' | 'PART_TIME' | 'INTERNSHIP';
  internshipDurationMonths?: number | null;
  recruiterEmail: string;
}

export interface Application {
  id: number;
  userId: number;
  jobId: number;
  jobTitle: string;
  applicantName?: string;
  applicantEmail: string;
  resumeUrl: string;
  offerLetterUrl?: string;
  company: string;
  status: 'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED' | 'OFFERED' | 'REJECTED';
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
  jobType?: 'FULL_TIME' | 'PART_TIME' | 'INTERNSHIP';
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

  forgotPassword(request: ForgotPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/change-password`, request);
  }

  logout(): void {
    this.clearSession();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();

    if (!token) {
      return false;
    }

    if (this.isTokenExpired(token)) {
      this.clearSession();
      return false;
    }

    return true;
  }

  getRole(): string | null {
    return this.getCurrentUser()?.role ?? null;
  }

  isAdmin(): boolean { return this.getRole() === 'ADMIN'; }
  isRecruiter(): boolean { return this.getRole() === 'RECRUITER'; }
  isJobSeeker(): boolean { return this.getRole() === 'JOB_SEEKER'; }

  private clearSession(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiryTime = payload.exp * 1000;
      return Date.now() >= expiryTime;
    } catch {
      return true;
    }
  }
}
