import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WeatherService {
  private weatherUrl = 'https://mazharhossain.com/api/v1/weather';

  constructor(private http: HttpClient) {}

  getWeather(): Observable<any> {
    return this.http.get(`${this.weatherUrl}`);
  }
}
