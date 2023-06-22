import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserExpensesService } from '../Services/user-expenses.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventTable, GoogleEvent } from '../models';

@Component({
  selector: 'app-expense-recurrence',
  templateUrl: './expense-recurrence.component.html',
  styleUrls: ['./expense-recurrence.component.css']
})
export class ExpenseRecurrenceComponent implements OnInit {


  isEFormValid : boolean = false;
  TYPE : string[] = ['Weekly', 'Monthly', 'Yearly']

  eventForm !: FormGroup;

  constructor(private router: Router, private userExpSvc: UserExpensesService, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.eventForm = this.createEvent()
  }

  addNew() {
    this.userExpSvc.handleCreateEvent(this.eventForm)
    this.userExpSvc.createNotification()
      .then(
        (p:any) => {
          console.info(">>> success: " + p["success"])
          this.router.navigate(['/summary'])
        }
      )
  }

  back() {
    this.router.navigate(['/record'])
  }

  createEvent() : FormGroup {
    return this.fb.group({
      frequency: this.fb.control<string>('Weekly', [Validators.required]),
      count: this.fb.control<number>(2, [Validators.required, Validators.min(2)]),
      summary: this.fb.control<string>('', [Validators.required]),
      description: this.fb.control<string>(this.userExpSvc.toSendTransaction.item),
      //@ts-ignore
      email: this.fb.control<string>(JSON.parse(localStorage.getItem('loginStatus')).email, [Validators.email])
    })
  }

}
