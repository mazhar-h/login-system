import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service'; // Adjust the path as necessary
import { Router } from '@angular/router'; // Import Router for navigation

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  passwordMatchError: boolean = false;
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(event: Event) {
    event.preventDefault(); // Prevent the default form submission
    const target = event.target as HTMLFormElement;
    const username = (target.elements.namedItem('username') as HTMLInputElement).value;
    const email = (target.elements.namedItem('email') as HTMLInputElement).value;
    const password = (target.elements.namedItem('password') as HTMLInputElement).value;
    const retypePassword = (target.elements.namedItem('retypePassword') as HTMLInputElement).value;

    if (password !== retypePassword) {
      this.passwordMatchError = true;
      return;
    }

    // Call the AuthService to register the user
    this.authService.register({ username, email, password }).subscribe({
      next: (response: any) => {
        // Handle successful registration
        console.log('Registration successful:', response);
        this.router.navigate(['/login']); // Navigate to login page
      },
      error: (error: any) => {
        // Handle error
        console.error('Registration failed:', error);
      }
    });
  }
}
