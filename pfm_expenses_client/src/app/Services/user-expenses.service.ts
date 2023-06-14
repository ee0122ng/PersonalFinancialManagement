import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { CURRENCY_API_URL, TRANSACTION_INSERT_API_URL, TRANSACTION_QUERY_API_URL } from '../constants';
import { Transaction } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserExpensesService {

  constructor(private http: HttpClient) { }

  getCurrencies() : Promise<any> {
    return lastValueFrom(this.http.get<any>(CURRENCY_API_URL))
  }

  addTransaction(form: FormGroup) : Promise<any> {
    const transaction : Transaction = {
      category: form.get('category')?.value,
      item: form.get('item')?.value,
      transactionDate: form.get('transactionDate')?.value,
      currency: form.get('currency')?.value,
      amount: form.get('amount')?.value
    } as Transaction

    return lastValueFrom(this.http.post<any>(TRANSACTION_INSERT_API_URL, transaction))
  }

  retrieveRecord(displayDate: Date) : Promise<any> {
    const params : HttpParams = new HttpParams().set("startDate", displayDate.toISOString());

    return lastValueFrom(this.http.get<any>(TRANSACTION_QUERY_API_URL, { params }));
  }
}
