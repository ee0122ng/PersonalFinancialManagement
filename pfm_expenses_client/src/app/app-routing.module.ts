import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivateComponent } from './Components/activate.component';
import { HomeComponent } from './Components/home.component';
import { LoginComponent } from './Components/login.component';
import { ProfileComponent } from './Components/profile.component';
import { RegisterComponent } from './Components/register.component';
import { AuthoriseAccountService } from './Services/authorise-account.service';
import { ExpenseSummaryComponent } from './Components/expense-summary.component';
import { ExpenseRecordComponent } from './Components/expense-record.component';
import { ExpenseTransactionComponent } from './Components/expense-transaction.component';
import { GoogleapiCallbackComponent } from './Components/googleapi-callback.component';

const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'login', component: LoginComponent},
  {path: 'activate/:accountId', component: ActivateComponent},
  {path: 'home', component: HomeComponent},
  {path: 'summary', component: ExpenseSummaryComponent},
  {path: 'record', component: ExpenseRecordComponent},
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
