import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivateComponent } from './Components/activate.component';
import { LoginComponent } from './Components/login.component';
import { ProfileComponent } from './Components/profile.component';
import { RegisterComponent } from './Components/register.component';
import { ExpenseSummaryComponent } from './Components/expense-summary.component';
import { ExpenseRecordComponent } from './Components/expense-record.component';
import { ExpenseTransactionComponent } from './Components/expense-transaction.component';
import { GoogleapiCallbackComponent } from './Components/googleapi-callback.component';
import { ExpenseRecurrenceComponent } from './Components/expense-recurrence.component';

const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'login', component: LoginComponent},
  {path: 'activate/:accountId', component: ActivateComponent},
  {path: 'summary', component: ExpenseSummaryComponent},
  {path: 'record', component: ExpenseRecordComponent},
  {path: 'recurrence', component: ExpenseRecurrenceComponent},
  {path: 'transaction', component: ExpenseTransactionComponent},
  {path: 'profile/:username', component: ProfileComponent},
  {path: 'callback', component: GoogleapiCallbackComponent},
  {path: '**', redirectTo: 'login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
