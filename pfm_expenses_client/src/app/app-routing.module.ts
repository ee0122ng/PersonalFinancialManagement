import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ActivateComponent } from './Components/activate.component';
import { HomeComponent } from './Components/home.component';
import { LoginComponent } from './Components/login.component';
import { ProfileComponent } from './Components/profile.component';
import { RegisterComponent } from './Components/register.component';
import { AuthoriseAccountService } from './Services/authorise-account.service';

const routes: Routes = [
  {path: 'register', component: RegisterComponent},
  {path: 'login', component: LoginComponent},
  {path: 'activate/:accountId', component: ActivateComponent, canActivate: [AuthoriseAccountService]},
  {path: 'home', component: HomeComponent},
  {path: 'profile', component: ProfileComponent, canActivate: [AuthoriseAccountService]},
  {path: '**', redirectTo: 'login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
