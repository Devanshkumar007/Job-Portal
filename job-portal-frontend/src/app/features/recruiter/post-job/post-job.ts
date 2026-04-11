import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { JobService } from '../../../core/services/job';
import { AuthService } from '../../../core/services/auth';
import { getHttpErrorMessage } from '../../../core/utils/http-error';

@Component({
  selector: 'app-post-job',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './post-job.html',
  styleUrl: './post-job.scss'
})
export class PostJob implements OnInit {
  postJobForm: FormGroup;
  isLoading = false;
  isEditMode = false;
  editingJobId: number | null = null;
  readonly jobTypeOptions = [
    { value: 'FULL_TIME', label: 'Full Time' },
    { value: 'PART_TIME', label: 'Part Time' },
    { value: 'INTERNSHIP', label: 'Internship' }
  ] as const;

  constructor(
    private fb: FormBuilder,
    private jobService: JobService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.postJobForm = this.fb.group({
      title: ['', Validators.required],
      companyName: ['', Validators.required],
      location: ['', Validators.required],
      jobType: ['FULL_TIME', Validators.required],
      salary: ['', [Validators.required, Validators.min(0)]],
      experience: ['', [Validators.required, Validators.min(0)]],
      internshipDurationMonths: [''],
      description: ['', [Validators.required, Validators.minLength(50)]],
    });

    this.postJobForm.get('jobType')?.valueChanges.subscribe((jobType) => {
      const internshipControl = this.postJobForm.get('internshipDurationMonths');
      const experienceControl = this.postJobForm.get('experience');

      if (!internshipControl || !experienceControl) return;

      if (jobType === 'INTERNSHIP') {
        internshipControl.setValidators([Validators.required, Validators.min(1)]);
        experienceControl.setValue(0);
      } else {
        internshipControl.reset('');
        internshipControl.clearValidators();
      }

      internshipControl.updateValueAndValidity();
    });
  }

  ngOnInit(): void {
    const jobId = this.route.snapshot.paramMap.get('id');
    if (!jobId) {
      return;
    }

    this.isEditMode = true;
    this.editingJobId = Number(jobId);
    this.loadJobForEdit(this.editingJobId);
  }

  onSubmit() {
    if (this.postJobForm.invalid) return;

    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.isLoading = true;

    const jobData = {
      ...this.postJobForm.value,
      salary: Number(this.postJobForm.value.salary),
      experience: Number(this.postJobForm.value.experience),
      internshipDurationMonths: this.postJobForm.value.jobType === 'INTERNSHIP'
        ? Number(this.postJobForm.value.internshipDurationMonths)
        : null,
      recruiterEmail: user.email
    };

    const request$ = this.isEditMode && this.editingJobId !== null
      ? this.jobService.updateJob(this.editingJobId, jobData, user.id)
      : this.jobService.createJob(jobData);

    request$.subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open(
          this.isEditMode ? 'Job updated successfully!' : 'Job posted successfully!',
          'Close',
          { duration: 3000 }
        );
        this.router.navigate(this.isEditMode && this.editingJobId !== null
          ? ['/jobs', this.editingJobId]
          : ['/recruiter/dashboard']);
      },
      error: (err: any) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: this.isEditMode
              ? 'Unable to update the job right now.'
              : 'Unable to post the job right now.',
            statusMessages: {
              400: 'Please review the job details and try again.',
              403: this.isEditMode
                ? 'You can only edit jobs that belong to you.'
                : 'You do not have permission to post jobs.',
              404: 'This job no longer exists.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  get isInternship(): boolean {
    return this.postJobForm.get('jobType')?.value === 'INTERNSHIP';
  }

  private loadJobForEdit(jobId: number): void {
    this.isLoading = true;
    this.jobService.getJobById(jobId).subscribe({
      next: (job) => {
        const currentUser = this.authService.getCurrentUser();
        if (!currentUser || currentUser.id !== job.recruiterId) {
          this.isLoading = false;
          this.snackBar.open('You can only edit jobs that belong to you.', 'Close', { duration: 3000 });
          this.router.navigate(['/jobs', jobId]);
          return;
        }

        this.postJobForm.patchValue({
          title: job.title,
          companyName: job.companyName,
          location: job.location,
          jobType: job.jobType ?? 'FULL_TIME',
          salary: job.salary,
          experience: job.experience,
          internshipDurationMonths: job.internshipDurationMonths ?? '',
          description: job.description
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.snackBar.open(
          getHttpErrorMessage(err, {
            defaultMessage: 'Unable to load this job for editing.',
            statusMessages: {
              403: 'You can only edit jobs that belong to you.',
              404: 'This job no longer exists.'
            }
          }),
          'Close',
          { duration: 3000 }
        );
        this.router.navigate(['/recruiter/dashboard']);
      }
    });
  }
}
