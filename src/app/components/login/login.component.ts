import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  errorMessage: string = '';
  userDisabled: boolean = false;
  username: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  onLogin(): void {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;

      this.authService.login(username, password).subscribe({
        next: (response) => {
          localStorage.setItem('token', response.accessToken);
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          if (error.status === 403 && error.error === 'User is not verified') {
            this.errorMessage = 'Your account is not verified. Please check your email to verify your account.';
            this.userDisabled = true;
            this.username = username;
          } else {
            this.errorMessage = 'Invalid credentials or login failed.';
          }
        },
      });
    }
  }

  resendVerification() {
    this.userService.resendVerificationLink(this.username).subscribe({
      next: () => {
        alert('Verification link has been resent to your email.');
      },
      error: () => {
        alert('Failed to resend the verification link.');
      },
    });
  }
}
