import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service'; // Adjust the path as necessary
import { Router } from '@angular/router'; // Import Router for navigation
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  passwordMatchError: boolean = false;
  
  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  onSubmit(event: Event) {
    event.preventDefault();
    const target = event.target as HTMLFormElement;
    const username = (target.elements.namedItem('username') as HTMLInputElement).value;
    const email = (target.elements.namedItem('email') as HTMLInputElement).value;
    const password = (target.elements.namedItem('password') as HTMLInputElement).value;
    const retypePassword = (target.elements.namedItem('retypePassword') as HTMLInputElement).value;

    if (password !== retypePassword) {
      this.passwordMatchError = true;
      return;
    }

    this.userService.register({ username, email, password }).subscribe({
      next: (response: any) => {
        this.router.navigate(['/login']);
      },
      error: (error: any) => {
        console.error('Registration failed:', error);
      }
    });
  }
}
