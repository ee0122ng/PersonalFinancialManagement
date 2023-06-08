import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './Components/register.component';
import { LoginComponent } from './Components/login.component';
import { ActivateComponent } from './Components/activate.component';
import { HomeComponent } from './Components/home.component';
import { AuthoriseAccountService } from './Services/authorise-account.service';
import { RetrieveProfileComponent } from './Components/retrieve-profile.component';

const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'login', component: LoginComponent},
  {path: 'activate/:accountId', component: ActivateComponent, canActivate: [AuthoriseAccountService]},
  {path: 'home', component: HomeComponent},
  {path: 'profile', component: RetrieveProfileComponent, canActivate: [AuthoriseAccountService]},
  {path: '**', redirectTo: 'login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
