import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';
import { environment } from 'src/app/environments/enivornment';

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
  showUsernameModal: boolean = false;
  googleId: string = '';
  email: string = '';
  usernameCreatedError: boolean = false;
  idToken: string = '';
  existingUsername: string = '';
  showUserLinkModal: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
    this.redirectDashboard();

    // @ts-ignore
    google.accounts.id.initialize({
      client_id:
        environment.googleOauthClientId,
      callback: this.handleCredentialResponse.bind(this),
      use_fedcm_for_prompt: true,
    });
    // @ts-ignore
    google.accounts.id.renderButton(document.getElementById('buttonDiv'), {
      theme: 'outline',
      size: 'large',
    });
  }

  handleCredentialResponse(response: any) {
    this.idToken = response.credential;
    this.authService.loginWithGoogle(this.idToken).subscribe({
      next: (resp) => {
        if (!resp.accessToken) {
          this.googleId = resp.googleId;
          this.email = resp.email;
          this.openUsernameModal();
        } else {
          localStorage.setItem('token', resp.accessToken);
          this.redirectDashboard();
        }
      },
      error: (err) => {
        if (err.error === 'User already exists') {
          this.authService.getExistingUser(this.idToken).subscribe({
            next: (resp) => {
              console.log(resp.username);
              this.existingUsername = resp.username;
              this.idToken = response.credential;
              this.showUserLinkModal = true;
              this.cdr.detectChanges();
            },
          });
        }
      },
    });
  }

  onModalClosed() {
    this.showUserLinkModal = false;
  }

  redirectDashboard(): void {
    let token = localStorage.getItem('token');
    if (token) this.router.navigate(['/dashboard']);
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
            this.errorMessage =
              'Your account is not verified. Please check your email to verify your account.';
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

  openUsernameModal() {
    this.showUsernameModal = true;
    this.cdr.detectChanges();
  }

  handleUsernameCreated(username: string) {
    this.authService
      .registerWithGoogle(this.idToken, {
        username: username,
      })
      .subscribe({
        next: (response) => {
          localStorage.setItem('token', response.accessToken);
          this.redirectDashboard();
          return;
        },
        error: (err) => {
          this.usernameCreatedError = true;
          this.cdr.detectChanges();
        },
      });
  }
}
