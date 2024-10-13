import { Component, EventEmitter, input, Input, Output } from '@angular/core';

@Component({
  selector: 'app-username-modal',
  templateUrl: './username-modal.component.html',
  styleUrls: ['./username-modal.component.css']
})
export class UsernameModalComponent {
  username: string  = '';
  showModal: boolean = true;
  @Input() usernameCreatedError: boolean = false;

  @Output() usernameCreated = new EventEmitter<string>();

  submitUsername() {
    if (this.username) {
      this.usernameCreated.emit(this.username);
    }
  }
}
