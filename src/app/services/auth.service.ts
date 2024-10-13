import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable } from 'rxjs';
import { environment } from '../environments/enivornment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient, private jwtHelper: JwtHelperService) {}

  loginWithGoogle(googleToken: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/google`, googleToken, { withCredentials: true })
  }

  registerWithGoogle(idToken: string, googleAuthRequest: { username: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/google/register`, googleAuthRequest , {  headers: { Authorization: 'Bearer ' + idToken }, withCredentials: true })
  }

  getExistingUser(idToken: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/auth/google/user`, { headers: { Authorization: 'Bearer ' + idToken } })
  }

  linkGoogleAccount(idToken: string, googleLinkRequest: {password: string}): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/google/link`, googleLinkRequest, {  headers: { Authorization: 'Bearer ' + idToken }, withCredentials: true })
  }
  
  login(username: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, { username, password }, { withCredentials: true });
  }

  register(registerData: { username: string; email: string; password: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, registerData, { responseType: 'text' });
  }

  refreshToken(): Observable<any> {
    return this.http.post(`${this.baseUrl}/refresh-token`, {}, { withCredentials: true });
  }

  processLogout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/logout`, {}, { headers: {"Authorization": "Bearer " + localStorage.getItem('token')}, withCredentials: true });
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    if (token && !this.jwtHelper.isTokenExpired(token))
      return true;
    else
      return false;
  }

  saveAccessToken(accessToken: string): void {
    localStorage.setItem('token', accessToken);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout() {
    this.processLogout().subscribe({});
    localStorage.removeItem('token');
  }
}