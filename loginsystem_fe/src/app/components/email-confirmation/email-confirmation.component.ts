import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-email-confirmation',
  templateUrl: './email-confirmation.component.html',
  styleUrls: ['./email-confirmation.component.css']
})
export class EmailConfirmationComponent implements OnInit {
  isLoading = true;
  isConfirmed = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.params['token'];
    if (token)
      this.confirmEmail(token);
  }

  confirmEmail(token: string): void {
    this.userService.confirmEmail(token)
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          this.isConfirmed = true;
        },
        error: (error) => {
          this.isLoading = false;
          this.isConfirmed = false;
        }
      });
  }
}