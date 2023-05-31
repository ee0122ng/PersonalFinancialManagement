import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { COUNTRY_API_URL } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class ActivateAccountService {

  constructor(private http: HttpClient) { }

  getCountriesList() : Promise<any> {
    return lastValueFrom(this.http.get<any>(COUNTRY_API_URL))
  }
}
