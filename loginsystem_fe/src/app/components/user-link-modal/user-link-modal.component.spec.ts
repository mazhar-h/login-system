import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserLinkModalComponent } from './user-link-modal.component';

describe('UserLinkModalComponent', () => {
  let component: UserLinkModalComponent;
  let fixture: ComponentFixture<UserLinkModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserLinkModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserLinkModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
