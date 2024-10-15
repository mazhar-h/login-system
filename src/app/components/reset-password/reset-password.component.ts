import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css'],
})
export class ResetPasswordComponent implements OnInit {
  newPassword: string = '';
  retypePassword: string = '';
  token: string | null = null;
  passwordMatchError: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token');
  }

  onSubmit() {
    if (this.token && (this.newPassword === this.retypePassword)) {
      this.userService
        .processResetPassword(this.token, this.newPassword)
        .subscribe({
          next: (response) => {
            console.log('Password reset successfully:', response);
            alert('Password has been reset successfully.');
            this.router.navigate(['/']);
          },
          error: (error) => {
            console.error('Error resetting password:', error);
            alert('Failed to reset password. Please try again.');
          },
        });
    }

    if (this.newPassword !== this.retypePassword)
      this.passwordMatchError = true;
  }
}
