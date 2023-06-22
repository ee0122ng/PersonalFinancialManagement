import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserExpensesService } from '../Services/user-expenses.service';
import { EventTable, GoogleEvent, Transaction } from '../models';
import { GoogleApiService } from '../Services/google-api.service';

@Component({
  selector: 'app-expense-transaction',
  templateUrl: './expense-transaction.component.html',
  styleUrls: ['./expense-transaction.component.css']
})
export class ExpenseTransactionComponent implements OnInit {

  CATEGORY: string[] = ['Income', 'Saving' ,'Spending'];
  CURRENCY!: string[];

  transactionForm !: FormGroup;
  today : Date = new Date()
  message !: string;
  isChecked : boolean = false;
  isFormValid : boolean = false;
  isTFormValid : boolean = false;

  constructor(private fb: FormBuilder, private router: Router, private userExpSvc: UserExpensesService, private googleApiSvc: GoogleApiService) { }

  ngOnInit(): void {

    this.transactionForm = this.createForm()

    this.userExpSvc.getCurrencies()
      .then(
        (p:any) => {
          this.CURRENCY = p["currencies"]
        }
      )
    
  }

  createForm() : FormGroup {
    if (!this.userExpSvc.toSendTransaction) {
      return this.fb.group({
        category: this.fb.control<string>('', [Validators.required]),
        item: this.fb.control<string>(''),
        amount: this.fb.control<number>(0, [Validators.required, Validators.min(0.01)]),
        transactionDate: this.fb.control<Date>(this.today, [Validators.required]),
        currency: this.fb.control<string>("SGD"),
      })
    } else {
      return this.fb.group({
        category: this.fb.control<string>(this.userExpSvc.toSendTransaction.category, [Validators.required]),
        item: this.fb.control<string>(this.userExpSvc.toSendTransaction.item),
        amount: this.fb.control<number>(this.userExpSvc.toSendTransaction.amount, [Validators.required, Validators.min(0.01)]),
        transactionDate: this.fb.control<Date>(this.userExpSvc.toSendTransaction.transactionDate, [Validators.required]),
        currency: this.fb.control<string>(this.userExpSvc.toSendTransaction.currency),
      })
    }

  }

  async addNew() {
    // use hanlders in service to store the transaction record
    await this.userExpSvc.handleCreateTransaction(this.transactionForm)

    if (!this.isChecked) {
      this.userExpSvc.addTransaction()
      .then(
        (p:any) => {
          console.info(">>> " + p["success"])
          this.transactionForm = this.createForm()
          this.router.navigate(['/summary'])
        }
      )
      .catch(
        (err:any) => {
          this.message = err["error"]["error"]
        }
      )
      
    } else {

      // route to child
      this.router.navigate(['/recurrence'])
    }
  }

  authenticateGoogleApi() {
    this.userExpSvc.handleCreateTransaction(this.transactionForm)
    this.googleApiSvc.invokeGoogleApi()
  }

  toggleRecur($event:any) {
    this.isChecked = !this.isChecked
  }

  checkFormValid() : boolean {

    return this.transactionForm.valid
  }

  handleClearItem() {
    this.transactionForm.get('item')?.reset()
  }

  handleClearAmount() {
    this.transactionForm.get('amount')?.reset()
  }

  hasAuthenticated() : Boolean {
    return !!sessionStorage.getItem('googleToken')
  }

}
