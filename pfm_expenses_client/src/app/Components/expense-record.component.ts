import { Component, OnInit } from '@angular/core';
import { UserExpensesService } from '../Services/user-expenses.service';
import { RecordTable, TransactionRecord } from '../models';

@Component({
  selector: 'app-expense-record',
  templateUrl: './expense-record.component.html',
  styleUrls: ['./expense-record.component.css']
})
export class ExpenseRecordComponent implements OnInit {

  today : Date = new Date();
  displayDate !: Date;
  transactionRecord : TransactionRecord[] = [];

  dataSource : RecordTable[] = []
  displayedColumns : string[] = ['position', 'id', 'category', 'item', 'itemDate', 'amount', 'currency']
  message !: string;

  constructor(private userExpSvc: UserExpensesService) {}

  ngOnInit(): void {

    if (!this.displayDate) {
      this.displayDate = this.today
    }

    this.getDatedRecords()

  }

  previous() {

    if (!!this.displayDate) {
      console.info(">>> display month: " + this.displayDate.getMonth())
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() - 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() - 1, 1)
    }
    console.info("to display date: " + this.displayDate)
    this.getDatedRecords()

  }

  next() {

    if (!!this.displayDate) {
      console.info(">>> display month: " + this.displayDate.getMonth())
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() + 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() + 1, 1)
    }
    console.info("to display date: " + this.displayDate)
    this.getDatedRecords()
  }

  prepareDataSource() {
    for (let i=0; i<this.transactionRecord.length; i++) {

      this.dataSource.push(
        {
          position: i + 1,
          id: this.transactionRecord[i].id,
          category: this.transactionRecord[i].category,
          item: this.transactionRecord[i].item,
          itemDate: this.transactionRecord[i].itemDate,
          amount: this.transactionRecord[i].amount,
          currency: this.transactionRecord[i].currency
        } as RecordTable
      )
    }
  }

  getDatedRecords() {
    this.userExpSvc.retrieveRecord(this.displayDate)
    .then(
      (p:any) => {
        return p["records"]
      }
    )
    .catch(
      (err:any) => {
        this.message = err["error"]["error"]
        console.info(">>> " + this.message)
      }
    )
    .then(
      (r:any[]) => {
        return r.map(
          (a:any) => {
            return {
              id: a["id"],
              category: a["category"],
              item: a["item"],
              itemDate: new Date(a["itemDate"]),
              currency: a["currency"],
              amount: a["amount"]
            } as TransactionRecord
          }
        )
      }
    ).then(
      (t:TransactionRecord[]) => {
        if (t.length > 0) {
          this.transactionRecord = t
          this.prepareDataSource()
        } else {
          this.transactionRecord = []
          this.dataSource  = []
        }
      }
    )
  }

}
