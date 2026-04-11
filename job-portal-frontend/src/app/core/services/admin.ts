import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PagedResponse } from './auth';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  phone?: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
  createdAt?: string;
}

export type UserRoleFilter = 'ALL' | 'ADMIN' | 'RECRUITER' | 'JOB_SEEKER';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  /**
   * Get all users with pagination and optional role filtering
   * @param page - Page number (0-indexed)
   * @param size - Page size
   * @param role - Filter by role (ALL returns all users, specific role uses /role/{role} endpoint)
   */
  getAllUsers(
    page: number = 0,
    size: number = 10,
    role?: UserRoleFilter
  ): Observable<PagedResponse<AdminUser>> {
    let url: string;

    if (role && role !== 'ALL') {
      // Use role-specific endpoint for filtered results
      url = `${this.apiUrl}/users/role/${role}?page=${page}&size=${size}`;
    } else {
      // Use general endpoint for all users
      url = `${this.apiUrl}/users?page=${page}&size=${size}`;
    }

    return this.http.get<PagedResponse<AdminUser>>(url);
  }

  /**
   * Delete a user by ID
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }
}
