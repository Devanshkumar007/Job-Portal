import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { Application } from '../../../../core/services/auth';

export type RecruiterApplicationStatus =
  'APPLIED' | 'UNDER_REVIEW' | 'SHORTLISTED' | 'INTERVIEW_SCHEDULED' | 'OFFERED' | 'REJECTED';

export interface StatusChangeDialogData {
  application: Application;
  nextStatus: RecruiterApplicationStatus;
}

export interface StatusChangeDialogResult {
  confirmed: boolean;
  status: RecruiterApplicationStatus;
  interviewLink?: string;
  interviewDate?: string;
  interviewTime?: string;
  timeZone?: string;
  offerLetterFile?: File;
}

@Component({
  selector: 'app-status-change-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule
  ],
  templateUrl: './status-change-dialog.html',
  styleUrl: './status-change-dialog.scss'
})
export class StatusChangeDialog {
  readonly form;
  readonly timeZoneOptions = [
    'Asia/Kolkata',
    'Asia/Dubai',
    'Asia/Singapore',
    'Asia/Tokyo',
    'Europe/London',
    'Europe/Berlin',
    'America/New_York',
    'America/Chicago',
    'America/Denver',
    'America/Los_Angeles',
    'Australia/Sydney'
  ];

  offerLetterFile: File | null = null;
  fileError = '';

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<StatusChangeDialog, StatusChangeDialogResult>,
    @Inject(MAT_DIALOG_DATA) public data: StatusChangeDialogData
  ) {
    this.form = this.fb.group({
      interviewLink: [''],
      interviewDate: [null as Date | null],
      interviewTime: [''],
      timeZone: [Intl.DateTimeFormat().resolvedOptions().timeZone || 'Asia/Kolkata']
    });

    if (this.requiresInterviewDetails) {
      this.form.patchValue({
        interviewDate: new Date(),
        interviewTime: '10:00'
      });
      this.form.get('interviewLink')?.setValidators([Validators.required]);
      this.form.get('interviewDate')?.setValidators([Validators.required]);
      this.form.get('interviewTime')?.setValidators([Validators.required]);
      this.form.get('timeZone')?.setValidators([Validators.required]);
      this.ensureTimeZoneOption();
      this.form.updateValueAndValidity();
    }
  }

  get requiresInterviewDetails(): boolean {
    return this.data.nextStatus === 'INTERVIEW_SCHEDULED';
  }

  get requiresOfferLetter(): boolean {
    return this.data.nextStatus === 'OFFERED';
  }

  get actionLabel(): string {
    return this.data.nextStatus === 'OFFERED' ? 'Send Offer' : 'Confirm';
  }

  get canSubmit(): boolean {
    if (this.requiresInterviewDetails) {
      return this.form.valid;
    }

    if (this.requiresOfferLetter) {
      return !!this.offerLetterFile;
    }

    return true;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.fileError = '';
    this.offerLetterFile = null;

    if (!file) {
      return;
    }

    const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      this.fileError = 'Upload a PDF offer letter.';
      input.value = '';
      return;
    }

    this.offerLetterFile = file;
  }

  cancel(): void {
    this.dialogRef.close({ confirmed: false, status: this.data.nextStatus });
  }

  confirm(): void {
    if (!this.canSubmit) {
      this.form.markAllAsTouched();
      if (this.requiresOfferLetter && !this.offerLetterFile) {
        this.fileError = 'Offer letter PDF is required.';
      }
      return;
    }

    const value = this.form.getRawValue();

    this.dialogRef.close({
      confirmed: true,
      status: this.data.nextStatus,
      interviewLink: value.interviewLink?.trim() || undefined,
      interviewDate: value.interviewDate ? this.toDateString(value.interviewDate) : undefined,
      interviewTime: value.interviewTime || undefined,
      timeZone: value.timeZone?.trim() || undefined,
      offerLetterFile: this.offerLetterFile ?? undefined
    });
  }

  private toDateString(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private ensureTimeZoneOption(): void {
    const selectedTimeZone = this.form.get('timeZone')?.value;
    if (
      typeof selectedTimeZone === 'string'
      && selectedTimeZone.trim()
      && !this.timeZoneOptions.includes(selectedTimeZone)
    ) {
      this.timeZoneOptions.unshift(selectedTimeZone);
    }
  }
}
