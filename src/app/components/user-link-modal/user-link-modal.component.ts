import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectorRef,
} from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-user-link-modal',
  templateUrl: './user-link-modal.component.html',
  styleUrls: ['./user-link-modal.component.css'],
})
export class UserLinkModalComponent {
  @Input() showModal: boolean = false;
  @Input() username: string = '';
  @Input() idToken: string = '';
  @Input() socialType: string = '';
  @Output() modalClosed: EventEmitter<void> = new EventEmitter<void>();

  password: string = '';
  errorMessage: string = '';

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  linkAccount() {
    console.log(this.socialType);
    if (this.socialType === 'google') {
      this.authService
        .linkGoogleAccount(this.idToken, { password: this.password })
        .subscribe({
          next: (response: any) => {
            this.closeModal();
            localStorage.setItem('token', response.accessToken);
            this.password = '';
            let token = localStorage.getItem('token');
            if (token) this.router.navigate(['/dashboard']);
          },
          error: (error: any) => {
            this.errorMessage = 'Password is incorrect. Please try again.';
            this.cdr.detectChanges();
          },
        });
    }

    if (this.socialType === 'facebook') {
      this.authService
        .linkFacebookAccount(this.idToken, { password: this.password })
        .subscribe({
          next: (response: any) => {
            this.closeModal();
            localStorage.setItem('token', response.accessToken);
            this.password = '';
            let token = localStorage.getItem('token');
            if (token) this.router.navigate(['/dashboard']);
          },
          error: (error: any) => {
            this.errorMessage = 'Password is incorrect. Please try again.';
            this.cdr.detectChanges();
          },
        });
    }
  }

  closeModal() {
    this.password = '';
    this.errorMessage = '';
    this.showModal = false;
    this.modalClosed.emit();
    this.cdr.detectChanges();
  }
}
