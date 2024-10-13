import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/enivornment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class WeatherService {
  private weatherUrl = environment.apiUrl + '/weather';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getWeather(): Observable<any> {
    return this.http.get(`${this.weatherUrl}`, { headers: { Authorization: 'Bearer ' + this.authService.getToken() } });
  }
}
