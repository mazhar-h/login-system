import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-forgot-username',
  templateUrl: './forgot-username.component.html',
  styleUrls: ['./forgot-username.component.css'],
})
export class ForgotUsernameComponent {
  email: string = '';

  constructor(private userService: UserService, private router: Router) {}

  onSubmit() {
    if (this.email) {
      this.userService.processForgotUsername(this.email).subscribe({
        next: (response) => {
          console.log('Username sent to email:', response);
          alert('Your username has been sent to your email address.');
          this.email = '';
        },
        error: (error) => {
          console.error('Error sending username:', error);
          alert('Failed to send username. Please try again.');
        },
      });
    }
  }
}
