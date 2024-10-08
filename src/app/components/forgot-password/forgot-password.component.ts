import { Component } from '@angular/core';

import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css'],
})
export class ForgotPasswordComponent {
  email: string = '';

  constructor(private userService: UserService) {}

  onSubmit() {
    if (this.email) {
      this.userService.processForgotPassword(this.email).subscribe({
        next: (response) => {
          console.log('Reset link sent:', response);
          alert('Reset link sent to your email.');
          this.email = '';
        },
        error: (error) => {
          console.error('Error sending reset link:', error);
          alert('Failed to send reset link. Please try again.');
        },
      });
    }
  }
}
