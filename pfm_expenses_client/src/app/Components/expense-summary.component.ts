import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { UserExpensesService } from '../Services/user-expenses.service';
import { RecordTable, TransactionRecord } from '../models';
import { MatTable } from '@angular/material/table';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-expense-summary',
  templateUrl: './expense-summary.component.html',
  styleUrls: ['./expense-summary.component.css']
})
export class ExpenseSummaryComponent implements OnInit {

  @ViewChild('incomeTable')
  incomeTable !: MatTable<RecordTable>;

  @ViewChild('expenseTable')
  expenseTable !: MatTable<RecordTable>;

  today : Date = new Date();
  displayDate !: Date;

  incomes : number = 0;
  spendings : number = 0;
  savings : number = 0;

  transactionRecord : TransactionRecord[] = [];
  displayedColumns : string[] = ['position', 'category', 'item', 'transactionDate', 'amount'];
  incomeSource : RecordTable[] = [];
  expenseSource : RecordTable[] = [];
  expSourceDisplay !: RecordTable[];

  summaryError : string = "";
  displayExpense : boolean = false;
  displayIncome : boolean = false;

  length : number = 0;
  pageSize : number = 5;
  pageIndex : number = 0;
  previousPageIndex !: number | undefined;
  pageSizeOptions : number[] = [5, 10]
  hidePageSize : boolean = false;
  showPageSizeOptions : boolean = true;
  showFirstLastButtons : boolean = true;
  disabled : boolean = false;
  pageEvent !: PageEvent;

  constructor(private userExpSvc : UserExpensesService) {}

  ngOnInit(): void {
      
    if (!this.displayDate) {
      this.displayDate = this.today
    }
    this.getDatedRecords()
  }

  getDatedRecords() {

    // get fresh records for every retrieval
    this.incomeSource = []
    this.expenseSource = []
    this.transactionRecord = []

    this.userExpSvc.getSummary(this.displayDate)
      .then(
        (p) => {
          this.incomes = p["income"]
          this.spendings = p["spending"]
          this.savings = this.incomes - this.spendings
          return p["summary"]["records"]
        }
      )
      .catch (
        (err:any) => {
          this.summaryError = err["error"]["error"]
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
                transactionDate: new Date(a["itemDate"]),
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
          }
        }
      )
  }

  prepareDataSource() {
    // sort the transaction records by date
    this.transactionRecord.sort((a, b) => {
      return <any> new Date(b.transactionDate) - <any>new Date(a.transactionDate)
    })

    let incomeRecords = this.transactionRecord.map(r => r.category).filter(c => c == 'income')
    let expenseRecords = this.transactionRecord.map(r => r.category).filter(c => c == 'spending')

    // prepare data table for income
    for (let i=0; i<incomeRecords.length; i++) {

      this.incomeSource.push(
        {
          position: i + 1,
          id: this.transactionRecord[i].id,
          category: this.transactionRecord[i].category,
          item: this.transactionRecord[i].item,
          transactionDate: this.transactionRecord[i].transactionDate,
          amount: this.transactionRecord[i].amount,
          currency: this.transactionRecord[i].currency
        }
      )

      if (!!this.incomeTable) {
        this.incomeTable.dataSource = this.incomeSource
        this.incomeTable.renderRows()
      }

    }
    // prepare data table for expense
    for (let i=0; i<expenseRecords.length; i++) {

      this.expenseSource.push(
        {
          position: i + 1,
          id: this.transactionRecord[i].id,
          category: this.transactionRecord[i].category,
          item: this.transactionRecord[i].item,
          transactionDate: this.transactionRecord[i].transactionDate,
          amount: this.transactionRecord[i].amount,
          currency: this.transactionRecord[i].currency
        }
      )
      this.expSourceDisplay = this.expenseSource.slice(this.pageIndex, this.pageSize)

      if (!!this.expenseTable) {
        this.expenseTable.dataSource = this.expenseSource.slice(this.pageIndex, this.pageSize)
        this.expenseTable.renderRows()
      }
    }
    this.length = this.expenseSource.length

  }

  handlePageEvent(e: PageEvent) {
    this.pageEvent = e
    this.pageSize = e.pageSize
    this.pageIndex = e.pageIndex
    this.previousPageIndex = e.previousPageIndex

    const startIndex = this.pageIndex * this.pageSize
    const endIndex = this.pageIndex * this.pageSize + this.pageSize
    this.expSourceDisplay = this.expenseSource.slice(startIndex, endIndex)
  }

  previous() {
    this.displayExpense = false;
    this.displayIncome = false;

    if (!!this.displayDate) {
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() - 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() - 1, 1)
    }
    this.getDatedRecords()

  }

  next() {
    this.displayExpense = false;
    this.displayIncome = false;
    
    if (!!this.displayDate) {
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() + 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() + 1, 1)
    }
    this.getDatedRecords()
  }

  onExpense() {
    this.displayExpense = !this.displayExpense
  }

  onIncome() {
    this.displayIncome = !this.displayIncome
  }

}
