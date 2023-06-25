import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { CURRENCY_API_URL, NOTIFICATION_CREATE_API_URL, RAILWAY_DOMAIN, TRANSACTION_DELETE_API_URL, TRANSACTION_INSERT_API_URL, TRANSACTION_QUERY_API_URL, TRANSACTION_SUMMARY_API_URL, TRANSACTION_UPDATE_API_URL } from '../constants';
import { GoogleEvent, Transaction, TransactionRecord } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserExpensesService {

  constructor(private http: HttpClient) { }

  toSendTransaction !: Transaction;
  toSendEvent !: GoogleEvent;

  $transactionCreate !: Promise<any>;

  getCurrencies() : Promise<any> {
    return lastValueFrom(this.http.get<any>(RAILWAY_DOMAIN+CURRENCY_API_URL))
    // return lastValueFrom(this.http.get<any>(CURRENCY_API_URL))
  }

  createNotification() : Promise<any> {
    const event = this.toSendEvent;
    this.toSendEvent = new GoogleEvent
    this.toSendTransaction = new Transaction
    return lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+NOTIFICATION_CREATE_API_URL, event))
    // return lastValueFrom(this.http.post<any>(NOTIFICATION_CREATE_API_URL, event))
  }

  async addTransaction() : Promise<any> {
    const transaction = this.toSendTransaction
    this.toSendTransaction = new Transaction
    return lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+TRANSACTION_INSERT_API_URL, transaction))
    // return lastValueFrom(this.http.post<any>(TRANSACTION_INSERT_API_URL, transaction))
  }

  retrieveRecord(displayDate: Date) : Promise<any> {
    const month : number = displayDate.getMonth()
    return lastValueFrom(this.http.get<any>(`${RAILWAY_DOMAIN}${TRANSACTION_QUERY_API_URL}/${month}`))
    // return lastValueFrom(this.http.get<any>(`${TRANSACTION_QUERY_API_URL}/${month}`))
  }

  getSummary(displayDate: Date) : Promise<any> {
    const month : number = displayDate.getMonth()
    const year : number = displayDate.getFullYear()
    return lastValueFrom(this.http.get<any>(`${RAILWAY_DOMAIN}${TRANSACTION_SUMMARY_API_URL}/${month}_${year}`))
    // return lastValueFrom(this.http.get<any>(`${TRANSACTION_SUMMARY_API_URL}/${month}`))
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
      const URL :string = `${RAILWAY_DOMAIN}${TRANSACTION_UPDATE_API_URL}?timestamp=${new Date().getTime()}`
      // const URL :string = `${TRANSACTION_UPDATE_API_URL}?timestamp=${new Date().getTime()}`

    return lastValueFrom(this.http.put<any>(URL, record))
  }

  deleteRecord(id: number) : Promise<any> {
    const params : HttpParams = new HttpParams().set("transactionId", id);

    return lastValueFrom(this.http.delete(RAILWAY_DOMAIN+TRANSACTION_DELETE_API_URL, { params } ))
    // return lastValueFrom(this.http.delete(TRANSACTION_DELETE_API_URL, { params } ))
  }

  handleCreateTransaction(form: FormGroup) {
    let transaction : Transaction = {
      category: form.get('category')?.value,
      item: form.get('item')?.value,
      transactionDate: form.get('transactionDate')?.value,
      currency: form.get('currency')?.value,
      amount: form.get('amount')?.value
    } as Transaction

    this.toSendTransaction = transaction;
  }

  handleCreateEvent(form: FormGroup) {
    let event : GoogleEvent = {
      frequency: form.get('frequency')?.value,
      count: form.get('count')?.value,
      summary: form.get('summary')?.value,
      description: form.get('description')?.value,
      email: form.get('email')?.value,
      startDate: this.toSendTransaction.transactionDate.toISOString(),
      category: this.toSendTransaction.category,
      item: this.toSendTransaction.item,
      amount: this.toSendTransaction.amount,
      transactionDate: this.toSendTransaction.transactionDate,
      currency: this.toSendTransaction.currency
    } as GoogleEvent

    this.toSendEvent = event
  }
}
