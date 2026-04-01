import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { ApplicationService } from '../../../core/services/application';
import { AuthService, Application } from '../../../core/services/auth';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  templateUrl: './my-applications.html',
  styleUrl: './my-applications.scss'
})
export class MyApplications implements OnInit {
  applications: Application[] = [];
  isLoading = true;

  constructor(
    private applicationService: ApplicationService,
    public authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.applicationService.getMyApplications(user.id).subscribe({
      next: (apps) => { this.applications = apps; this.isLoading = false; },
      error: () => { this.isLoading = false; this.snackBar.open('Failed to load applications.', 'Close', { duration: 3000 }); }
    });
  }

  getStatusColor(status: string): string {
    const map: any = { APPLIED: 'primary', UNDER_REVIEW: 'accent', SHORTLISTED: 'warn', REJECTED: '' };
    return map[status] || 'primary';
  }

  getStatusIcon(status: string): string {
    const map: any = { APPLIED: 'send', UNDER_REVIEW: 'hourglass_empty', SHORTLISTED: 'star', REJECTED: 'cancel' };
    return map[status] || 'info';
  }
}