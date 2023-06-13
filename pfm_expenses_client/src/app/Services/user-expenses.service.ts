import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { TRANSACTION_API_URL } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class UserExpensesService {

  constructor(private http: HttpClient) { }

  getCurrencies() : Promise<any> {
    return lastValueFrom(this.http.get<any>(TRANSACTION_API_URL))
  }
}
