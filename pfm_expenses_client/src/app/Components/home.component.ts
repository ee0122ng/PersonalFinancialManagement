import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserExpensesService } from '../Services/user-expenses.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  CATEGORY: string[] = ['Income', 'Expenses'];
  CURRENCY!: string[]; 

  transactionForm !: FormGroup;
  today : Date = new Date()

  constructor(private fb: FormBuilder, private router: Router, private userExpSvc: UserExpensesService) { }

  ngOnInit(): void {
    console.info(">>> you have re-initiate the page...")

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
      currency: this.fb.control<string>("SGD")
    })
  }

}
