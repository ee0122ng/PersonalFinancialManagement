import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserExpensesService } from '../Services/user-expenses.service';
import { EventTable, GoogleEvent } from '../models';

@Component({
  selector: 'app-expense-transaction',
  templateUrl: './expense-transaction.component.html',
  styleUrls: ['./expense-transaction.component.css']
})
export class ExpenseTransactionComponent implements OnInit {

  CATEGORY: string[] = ['Income', 'Saving' ,'Spending'];
  CURRENCY!: string[]; 
  TYPE : string[] = ['Weekly', 'Monthly', 'Yearly']

  transactionForm !: FormGroup;
  today : Date = new Date()
  message !: string;
  isChecked : boolean = false;
  isFormValid : boolean = false;
  isTFormValid : boolean = false;
  isEFormValid : boolean = false;

  eventForm !: FormGroup;
  eventSource : EventTable[] = [{
    frequency: '',
    count: 0,
    summary: '',
    description: '',
    //@ts-ignore
    email: '',
  }]
  displayedColumns : string[] = ['frequency', 'count', 'summary', 'description', 'email']

  constructor(private fb: FormBuilder, private router: Router, private userExpSvc: UserExpensesService) { }

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
    return this.fb.group({
      category: this.fb.control<string>('', [Validators.required]),
      item: this.fb.control<string>(''),
      amount: this.fb.control<number>(0, [Validators.required, Validators.min(0.01)]),
      transactionDate: this.fb.control<Date>(this.today, [Validators.required]),
      currency: this.fb.control<string>("SGD"),
      recurring: this.fb.control<boolean>(false)
    })
  }

  addNew() {
    this.userExpSvc.addTransaction(this.transactionForm)
      .then(
        (p:any) => {
          console.info(">>> " + p["success"])
          this.transactionForm.reset()
          this.router.navigate(['/summary'])
        }
      )
      .catch(
        (err:any) => {
          this.message = err["error"]["error"]
        }
      )
    
    if (this.isChecked) {
      console.info(">>> creating notification...")
      
      let event : GoogleEvent = {
        frequency: this.eventForm.get('frequency')?.value,
        count: this.eventForm.get('count')?.value,
        summary: this.eventForm.get('summary')?.value,
        description: this.eventForm.get('description')?.value,
        email: this.eventForm.get('email')?.value,
        startDate: this.transactionForm.get('transactionDate')?.value
      } as GoogleEvent

      this.userExpSvc.createNotification(event)
        .then(
          (p:any) => {
            console.info(">>> success: " + p["success"])
          }
        )
    }
  }

  toggleRecur($event:any) {
    this.isChecked = !this.isChecked
    this.eventForm = this.createEvent()
  }

  checkFormValid() : boolean {
    this.isTFormValid = this.transactionForm.valid;

    if (this.isChecked) {
      this.isEFormValid = this.eventForm.valid;
    }

    this.isFormValid = this.isTFormValid && (!this.isChecked || this.isEFormValid)

    return this.isFormValid
  }

  createEvent() : FormGroup {
    return this.fb.group({
      frequency: this.fb.control<string>('Weekly', [Validators.required]),
      count: this.fb.control<number>(2, [Validators.required, Validators.min(2)]),
      summary: this.fb.control<string>('', [Validators.required]),
      description: this.fb.control<string>(''),
      //@ts-ignore
      email: this.fb.control<string>(JSON.parse(localStorage.getItem('loginStatus')).email, [Validators.email])
    })
  }

}
