import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  // Default — go to login first
  { path: '', redirectTo: '/jobs', pathMatch: 'full' },

  // Public routes
  { path: 'login', loadComponent: () => import('./features/auth/login/login').then(m => m.Login) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register').then(m => m.Register) },
  { path: 'forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./features/auth/reset-password/reset-password').then(m => m.ResetPassword) },

  // Job routes (login required)
  { path: 'jobs', loadComponent: () => import('./features/jobs/job-list/job-list').then(m => m.JobList), canActivate: [authGuard] },
  { path: 'jobs/search', loadComponent: () => import('./features/jobs/job-search/job-search').then(m => m.JobSearch), canActivate: [authGuard] },
  { path: 'jobs/:id', loadComponent: () => import('./features/jobs/job-detail/job-detail').then(m => m.JobDetail), canActivate: [authGuard] },

  // Job Seeker routes
  { path: 'my-applications', loadComponent: () => import('./features/applications/my-applications/my-applications').then(m => m.MyApplications), canActivate: [authGuard, roleGuard], data: { roles: ['JOB_SEEKER'] } },
  { path: 'profile', loadComponent: () => import('./features/profile/profile/profile').then(m => m.Profile), canActivate: [authGuard] },
  { path: 'profile/change-password', loadComponent: () => import('./features/profile/change-password/change-password').then(m => m.ChangePassword), canActivate: [authGuard] },
  { path: 'users/email', loadComponent: () => import('./features/profile/profile/profile').then(m => m.Profile), canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN', 'RECRUITER'] } },

  // Recruiter routes
  { path: 'recruiter/dashboard', loadComponent: () => import('./features/recruiter/recruiter-dashboard/recruiter-dashboard').then(m => m.RecruiterDashboard), canActivate: [authGuard, roleGuard], data: { roles: ['RECRUITER'] } },
  { path: 'recruiter/post-job', loadComponent: () => import('./features/recruiter/post-job/post-job').then(m => m.PostJob), canActivate: [authGuard, roleGuard], data: { roles: ['RECRUITER'] } },
  { path: 'recruiter/jobs/:id/edit', loadComponent: () => import('./features/recruiter/post-job/post-job').then(m => m.PostJob), canActivate: [authGuard, roleGuard], data: { roles: ['RECRUITER'] } },
  { path: 'recruiter/applications', loadComponent: () => import('./features/recruiter/recruiter-applications/recruiter-applications').then(m => m.RecruiterApplications), canActivate: [authGuard, roleGuard], data: { roles: ['RECRUITER'] } },

  // Admin routes
  { path: 'admin/users', loadComponent: () => import('./features/admin/admin-users/admin-users').then(m => m.AdminUsers), canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/jobs', loadComponent: () => import('./features/admin/admin-jobs/admin-jobs').then(m => m.AdminJobs), canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/applications', loadComponent: () => import('./features/admin/admin-applications/admin-applications').then(m => m.AdminApplications), canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN'] } },

  // Fallback
  { path: '**', redirectTo: '/jobs' }
];
