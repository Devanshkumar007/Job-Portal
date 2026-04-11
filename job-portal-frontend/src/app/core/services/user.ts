import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from './auth';

@Injectable({ providedIn: 'root' })
export class UserService {
  private apiUrl = 'http://localhost:8080/api/user';

  constructor(private http: HttpClient) {}

  getMyProfile(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  updateMyProfile(payload: {
    name?: string;
    phone?: string;
    email?: string;
    password?: string;
    currentPassword?: string;
  }): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/me`, payload);
  }

  getUserByEmail(email: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/email/${encodeURIComponent(email)}`);
  }
}
