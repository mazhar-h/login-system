import { Component, OnInit, ViewChild } from '@angular/core';
import { UserService } from 'src/app/services/user.service';
import { DeleteAccountModalComponent } from '../delete-account-modal/delete-account-modal.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
})
export class SettingsComponent implements OnInit {
  email: string = '';
  currentEmail: string = '';
  password: string = '';
  currentPassword: string = '';
  retypePassword: string = '';
  initiatedUpdateEmail: boolean = false;
  changedPassword: boolean = false;
  errorPassword: boolean = false;
  passwordMatchError: boolean = false;
  
  @ViewChild(DeleteAccountModalComponent) deleteAccountModal!: DeleteAccountModalComponent;

  constructor(private userService: UserService) {}
  ngOnInit(): void {
    this.getEmail();
  }

  getEmail() {
    this.userService.getUserEmail().subscribe({
      next: (response) => {
        this.currentEmail = response.email;
      },
    });
  }

  updateEmail() {
    const data = { email: this.email };
    this.userService.updateUserEmail(data).subscribe({
      next: (response) => {
        this.initiatedUpdateEmail = true;
      },
    });
  }

  updatePassword() {
    if (this.password === this.retypePassword) {
      const data = {
        currentPassword: this.currentPassword,
        newPassword: this.password,
      };
      this.userService.updateUserPassword(data).subscribe({
        next: (response) => {
          this.password = '';
          this.currentPassword = '';
          this.retypePassword = '';
          this.errorPassword = false;
          this.passwordMatchError = false;
          this.changedPassword = true;
        },
        error: (err) => {
          this.changedPassword = false;
          this.passwordMatchError = false;
          this.errorPassword = true;
        }
      });
    } else {
      this.errorPassword = false;
      this.changedPassword = false;
      this.passwordMatchError = true;
    }
  }

  openDeleteModal() {
    this.deleteAccountModal.openModal();
  }

  handleDelete() {
    this.userService.deleteUser2().subscribe((response) => {
      alert('Account deleted successfully.');
    });
  }

  handleCancel() {
  }
}
