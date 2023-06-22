import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserExpensesService } from '../Services/user-expenses.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventTable, GoogleEvent } from '../models';
import { GoogleApiService } from '../Services/google-api.service';

@Component({
  selector: 'app-expense-recurrence',
  templateUrl: './expense-recurrence.component.html',
  styleUrls: ['./expense-recurrence.component.css']
})
export class ExpenseRecurrenceComponent implements OnInit {


  isEFormValid : boolean = false;
  TYPE : string[] = ['Weekly', 'Monthly', 'Yearly']

  eventForm !: FormGroup;
  email : string = "";

  errorMessage !: string;

  constructor(private router: Router, private userExpSvc: UserExpensesService, private fb: FormBuilder, private googleApiSvc: GoogleApiService) {}

  ngOnInit(): void {
    //@ts-ignore
    this.email = JSON.parse(localStorage.getItem('loginStatus')).email
    this.eventForm = this.createEvent()
  }

  addNew() {
    this.userExpSvc.handleCreateEvent(this.eventForm)
    this.userExpSvc.addTransaction()
      .then(
        (p:any) => {
          this.userExpSvc.createNotification()
          .then(
            (p:any) => {
              console.info(">>> success: " + p["success"])
              this.router.navigate(['/summary'])
            }
          )
        }
      )
      .catch(
        (err:any) => {
          (err:any) => {
            this.errorMessage = err["error"]["error"]
          }
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
      description: this.fb.control<string>(''), //this.userExpSvc.toSendTransaction.item
      //@ts-ignore
      email: this.fb.control<string>(this.email, [Validators.email])
    })
  }

}
