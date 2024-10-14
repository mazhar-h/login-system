import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataDeletionPolicyComponent } from './data-deletion-policy.component';

describe('DataDeletionPolicyComponent', () => {
  let component: DataDeletionPolicyComponent;
  let fixture: ComponentFixture<DataDeletionPolicyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataDeletionPolicyComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataDeletionPolicyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
