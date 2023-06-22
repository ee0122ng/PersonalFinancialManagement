import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { UserExpensesService } from '../Services/user-expenses.service';
import { RecordTable, TransactionRecord } from '../models';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTable } from '@angular/material/table';

@Component({
  selector: 'app-expense-record',
  templateUrl: './expense-record.component.html',
  styleUrls: ['./expense-record.component.css']
})
export class ExpenseRecordComponent implements OnInit {

  @ViewChild('recordTable')
  recordTable !: MatTable<RecordTable>;

  CATEGORY : string[] = ['income', 'spending', 'saving']
  CURRENCY: string[] = []

  today : Date = new Date();
  displayDate !: Date;
  transactionRecord : TransactionRecord[] = [];

  dataSource : RecordTable[] = []
  displayedColumns : string[] = ['position', 'category', 'item', 'transactionDate', 'amount', 'currency', 'actions']
  retrieveError !: string;

  editting : Boolean = false;
  editError !: string;
  editSuccess !: string;
  editForm !: FormGroup;
  editSource : RecordTable[] = []

  deleteSuccess !: string;
  deleteError !: string;

  constructor(private userExpSvc: UserExpensesService, private fb: FormBuilder) {}

  ngOnInit(): void {

    if (!this.displayDate) {
      this.displayDate = this.today
    }

    this.getDatedRecords()

  }

  previous() {
    this.editting = false
    this.editSuccess = ""

    if (!!this.displayDate) {
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() - 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() - 1, 1)
    }
    this.getDatedRecords()

  }

  next() {
    this.editting = false
    this.editSuccess = ""

    if (!!this.displayDate) {
      this.displayDate = new Date(this.displayDate.getFullYear(), this.displayDate.getMonth() + 1, 1)
    } else {
      this.displayDate = new Date(this.today.getFullYear(), this.today.getMonth() + 1, 1)
    }
    this.getDatedRecords()
  }

  prepareDataSource() {
    // sort the transaction records by date
    this.transactionRecord.sort((a, b) => {
      return <any> new Date(b.transactionDate) - <any>new Date(a.transactionDate)
    })

    for (let i=0; i<this.transactionRecord.length; i++) {

      this.dataSource.push(
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

      if (!!this.recordTable) {
        this.recordTable.dataSource = this.dataSource
        this.recordTable.renderRows()
      }

    }

  }

  getDatedRecords() {
    // get fresh records for every retrieval
    this.dataSource = []
    this.editSource = []
    this.transactionRecord = []

    this.userExpSvc.retrieveRecord(this.displayDate)
    .then(
      (p:any) => {
        return p["records"]
      }
    )
    .catch(
      (err:any) => {
        this.retrieveError = err["error"]["error"]
        console.info(">>> error retrieve record: " + JSON.stringify(err['error']))
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

  openEditWindow(id: number) {
    //@ts-ignore
    const item : RecordTable = this.dataSource.at(this.dataSource.findIndex(d => d.id == id))
    this.editSource.push(item);

    if (this.CURRENCY.length <= 0) {
      this.userExpSvc.getCurrencies()
        .then(
          (p:any) => {
            this.CURRENCY = p["currencies"]
          }
        )
    }

    this.editting = true
    this.editForm = this.fb.group({
      id: this.fb.control<number>(id),
      position: this.fb.control<number>(item.position),
      category: this.fb.control<string>(item.category, [Validators.required]),
      item: this.fb.control<string>(item.item),
      transactionDate: this.fb.control<Date>(item.transactionDate, [Validators.required]),
      amount: this.fb.control<number>(item.amount, [Validators.required]),
      currency: this.fb.control<string>(item.currency, [Validators.required])
    })

  }

  editRecord() {
    // reset the message 
    this.editError = ""
    this.editSuccess = ""

    this.userExpSvc.editRecord(this.editForm)
      .then(
        async (p:any) => {
          this.editSuccess = p["success"]

          // reset cached data
          this.editting = false
          this.editForm.reset()
          await this.getDatedRecords()
          console.info('>>> data source updated?: ' + JSON.stringify(this.dataSource))
        }
      )
      .catch(
        (err:any) => {
          this.editError = err["error"]["fail"]
          console.info(">>> update error: " + this.editError)
        }
      )
      .catch (
        (err:any) => {
          this.editError = err["error"]["error"]
          console.info(">>> update error: " + this.editError)
        }
      )
  }

  deleteRecord(id: number) {
    this.userExpSvc.deleteRecord(id)
      .then(
        async (p:any) => {
          this.deleteSuccess = p["success"]
          await this.getDatedRecords()
        }
      )
      .catch (
        (err:any) => {
          this.deleteError = err["error"]["fail"]
          console.info(">>> delete error: " + this.deleteError)
        }
      )
      .catch (
        (err:any) => {
          this.deleteError = err["error"]["error"]
          console.info(">>> delete error: " + this.deleteError)
        }
      )
  }

  cancelEdit() {
    this.editForm.reset()
    this.editting = false
    this.editSource = []
  }

}
