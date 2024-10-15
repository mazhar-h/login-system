import { Component, EventEmitter, input, Input, Output } from '@angular/core';

@Component({
  selector: 'app-username-modal',
  templateUrl: './username-modal.component.html',
  styleUrls: ['./username-modal.component.css']
})
export class UsernameModalComponent {
  username: string  = '';
  showModal: boolean = true;
  @Input() googleUsernameCreatedError: boolean = false;
  @Input() facebookUsernameCreatedError: boolean = false;
  @Input() socialType = '';

  @Output() googleUsernameCreated = new EventEmitter<string>();
  @Output() facebookUsernameCreated = new EventEmitter<string>();

  submitGoogleUsername() {
    if (this.username) {
      this.googleUsernameCreated.emit(this.username);
    }
  }

  submitFacebookUsername() {
    if (this.username) {
      this.facebookUsernameCreated.emit(this.username);
    }
  }
}
