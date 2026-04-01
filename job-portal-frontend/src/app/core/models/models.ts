export interface User {
  id: number;
  name: string;
  email: string;
  phone?: string;
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
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  appliedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}