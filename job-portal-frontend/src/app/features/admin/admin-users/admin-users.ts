import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers implements OnInit {
  users: any[] = [];
  isLoading = true;
  displayedColumns = ['name', 'email', 'role', 'phone', 'actions'];

  constructor(private http: HttpClient, private snackBar: MatSnackBar) {}

  ngOnInit() { this.loadUsers(); }

  loadUsers() {
    this.isLoading = true;
    this.http.get<any>('http://localhost:8080/api/admin/users').subscribe({
      next: (res) => { this.users = res.content ?? res; this.isLoading = false; },
      error: () => { this.isLoading = false; this.snackBar.open('Failed to load users.', 'Close', { duration: 3000 }); }
    });
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user?')) return;
    this.http.delete(`http://localhost:8080/api/admin/users/${id}`).subscribe({
      next: () => { this.snackBar.open('User deleted!', 'Close', { duration: 3000 }); this.loadUsers(); },
      error: () => this.snackBar.open('Failed to delete user.', 'Close', { duration: 3000 })
    });
  }

  getRoleColor(role: string): string {
    return role === 'ADMIN' ? 'warn' : role === 'RECRUITER' ? 'accent' : 'primary';
  }
}