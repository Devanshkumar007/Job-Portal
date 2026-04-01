import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecruiterApplications } from './recruiter-applications';

describe('RecruiterApplications', () => {
  let component: RecruiterApplications;
  let fixture: ComponentFixture<RecruiterApplications>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecruiterApplications],
    }).compileComponents();

    fixture = TestBed.createComponent(RecruiterApplications);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
