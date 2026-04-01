import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminApplications } from './admin-applications';

describe('AdminApplications', () => {
  let component: AdminApplications;
  let fixture: ComponentFixture<AdminApplications>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminApplications],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminApplications);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
