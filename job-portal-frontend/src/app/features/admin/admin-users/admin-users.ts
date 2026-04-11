import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { getHttpErrorMessage } from '../../../core/utils/http-error';
import { AdminService, UserRoleFilter } from '../../../core/services/admin';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatPaginatorModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers implements OnInit {
  users: any[] = [];
  isLoading = true;
  displayedColumns = ['name', 'email', 'role', 'phone', 'actions'];
  selectedRole: UserRoleFilter = 'ALL';
  totalUsers = 0;
  pageSize = 10;
  currentPage = 0;
  searchEmail = '';
  searchMode = false;
  searchLoading = false;
  searchOpen = false;
  readonly roleOptions: Array<{ value: UserRoleFilter; label: string }> = [
    { value: 'ALL', label: 'All Users' },
    { value: 'ADMIN', label: 'Admins' },
    { value: 'RECRUITER', label: 'Recruiters' },
    { value: 'JOB_SEEKER', label: 'Job Seekers' }
  ];

  constructor(
    private adminService: AdminService,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit() { this.loadUsers(); }

  loadUsers() {
    this.isLoading = true;
    this.adminService.getAllUsers(this.currentPage, this.pageSize, this.selectedRole).subscribe({
      next: (res) => {
        this.users = res.content ?? res;
        this.totalUsers = res.totalElements ?? this.users.length;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to load users right now.',
            statusMessages: {
              403: 'You do not have permission to view users.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user?')) return;
    this.adminService.deleteUser(id).subscribe({
      next: () => { this.snackBar.open('User deleted!', 'Close', { duration: 3000 }); this.loadUsers(); },
      error: (err) => this.snackBar.open(
        getHttpErrorMessage(err, {
          defaultMessage: 'Unable to delete this user right now.',
          statusMessages: {
            403: 'You do not have permission to delete this user.',
            404: 'This user no longer exists.'
          }
        }),
        'Close',
        { duration: 3000 }
      )
    });
  }

  getRoleColor(role: string): string {
    return role === 'ADMIN' ? 'warn' : role === 'RECRUITER' ? 'accent' : 'primary';
  }

  onRoleChange(role: UserRoleFilter): void {
    this.selectedRole = role;
    this.currentPage = 0; // Reset to first page when role changes
    this.loadUsers();
  }

  get filteredUsers(): any[] {
    return this.users; // Data is already filtered from API
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  viewUserProfile(email: string): void {
    if (!email?.trim()) {
      return;
    }

    this.router.navigate(['/users/email'], { queryParams: { email } });
  }

  searchByEmail(): void {
    if (!this.searchEmail?.trim()) {
      this.snackBar.open('Please enter an email address', 'Close', { duration: 3000 });
      return;
    }

    this.searchLoading = true;
    this.userService.getUserByEmail(this.searchEmail).subscribe({
      next: (user) => {
        this.users = [user];
        this.totalUsers = 1;
        this.searchMode = true;
        this.searchLoading = false;
      },
      error: (err) => {
        this.searchLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'User not found',
            statusMessages: {
              404: 'No user found with this email address.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  clearSearch(): void {
    this.searchEmail = '';
    this.searchMode = false;
    this.currentPage = 0;
    this.loadUsers();
  }

  toggleSearch(): void {
    this.searchOpen = !this.searchOpen;
    if (!this.searchOpen) {
      this.clearSearch();
    }
  }
}
