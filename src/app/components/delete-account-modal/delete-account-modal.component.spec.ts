import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteAccountModalComponent } from './delete-account-modal.component';

describe('DeleteAccountModalComponent', () => {
  let component: DeleteAccountModalComponent;
  let fixture: ComponentFixture<DeleteAccountModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteAccountModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeleteAccountModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
