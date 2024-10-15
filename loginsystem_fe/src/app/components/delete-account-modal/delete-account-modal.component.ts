import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-delete-account-modal',
  templateUrl: './delete-account-modal.component.html',
  styleUrls: ['./delete-account-modal.component.css']
})
export class DeleteAccountModalComponent {
  showModal = false;

  @Output() confirmDelete = new EventEmitter<void>();
  @Output() cancelDelete = new EventEmitter<void>();

  openModal() {
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  onConfirm() {
    this.confirmDelete.emit();
    this.closeModal();
  }

  onCancel() {
    this.cancelDelete.emit();
    this.closeModal();
  }
}