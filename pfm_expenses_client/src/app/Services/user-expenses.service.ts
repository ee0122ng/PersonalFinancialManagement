import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { CURRENCY_API_URL, NOTIFICATION_CREATE_API_URL, TRANSACTION_DELETE_API_URL, TRANSACTION_INSERT_API_URL, TRANSACTION_QUERY_API_URL, TRANSACTION_SUMMARY_API_URL, TRANSACTION_UPDATE_API_URL } from '../constants';
import { EventTable, GoogleEvent, Transaction, TransactionRecord } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserExpensesService {

  constructor(private http: HttpClient) { }

  getCurrencies() : Promise<any> {
    return lastValueFrom(this.http.get<any>(CURRENCY_API_URL))
  }

  createNotification(event : GoogleEvent) : Promise<any> {
    return lastValueFrom(this.http.post<any>(NOTIFICATION_CREATE_API_URL, event))
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
    const month : number = displayDate.getMonth()
    return lastValueFrom(this.http.get<any>(`${TRANSACTION_QUERY_API_URL}/${month}`))
  }

  getSummary(displayDate: Date) : Promise<any> {
    const month : number = displayDate.getMonth()
    return lastValueFrom(this.http.get<any>(`${TRANSACTION_SUMMARY_API_URL}/${month}`))
  }

  editRecord(form: FormGroup) : Promise<any> {
    const record : TransactionRecord = {
      id: form.get('id')?.value,
      category: form.get('category')?.value,
      item: form.get('item')?.value,
      transactionDate: form.get('transactionDate')?.value,
      amount: form.get('amount')?.value,
      currency: form.get('currency')?.value
    } as TransactionRecord

      // add cache busting mechanism
      const URL :string = `${TRANSACTION_UPDATE_API_URL}?timestamp=${new Date().getTime()}`

    return lastValueFrom(this.http.put<any>(URL, record))
  }

  deleteRecord(id: number) : Promise<any> {
    const params : HttpParams = new HttpParams().set("transactionId", id);

    return lastValueFrom(this.http.delete(TRANSACTION_DELETE_API_URL, { params } ))
  }
}
