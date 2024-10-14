import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/enivornment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  login(): Observable<any> {
    return this.http.get<string>(`${this.baseUrl}/hello`, this.getOptions());
  }

  register(registerData: { username: string; email: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/register`, registerData, { responseType: 'text' });
  }

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/users`);
  }

  getUser(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users`);
  }

  getUserEmail(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/email`);
  }

  createUser(user: { username: string; email: string; role: string }): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/users`, user, {responseType: 'text' as 'json'});
  }

  updateUser(user: { currentUsername: string | null; newUsername: string | null; newEmail: string | null; newRole: string | null }): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/admin/users`, user, {responseType: 'text' as 'json'});
  }

  confirmEmail(token: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/confirm-email`, { token: token } , {responseType: 'text' as 'json'});
  }

  updateUserEmail(user: { email: string | null }): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/users/update-email`, user, {responseType: 'text' as 'json'});
  }

  updateUserPassword(user: { currentPassword: string | null, newPassword: string | null }): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/users/password`, user, {responseType: 'text' as 'json'});
  }

  deleteUser(username: string): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/admin/users/${username}`, this.getOptions());
  }

  deleteUser2(): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/users`, this.getOptions());
  }

  processForgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/forgot-password`, {email}, {responseType: 'text' as 'json'});
  }

  processResetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/reset-password`, { token, newPassword }, {responseType: 'text' as 'json'});
  }

  processForgotUsername(email: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/forgot-username`, {email}, {responseType: 'text' as 'json'});
  }

  resendVerificationLink(username: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/resend-verification`, { username }, {responseType: 'text' as 'json'});
  }

  verifyEmail(token: string): Observable<any[]> {
    return this.http.get<any>(`${this.baseUrl}/users/verify-email?token=${token}`, {responseType: 'text' as 'json'});
  }

  private getOptions() {
    let headers_object = new HttpHeaders({
      'Content-Type': 'application/json',
      "Authorization": "Bearer " + localStorage.getItem('token')
    });
    
    const httpOptions = {
      headers: headers_object,
      responseType: 'text'as'json'
    };

    return httpOptions;
  }
}