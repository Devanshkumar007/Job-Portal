import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminJobs } from './admin-jobs';

describe('AdminJobs', () => {
  let component: AdminJobs;
  let fixture: ComponentFixture<AdminJobs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminJobs],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminJobs);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
