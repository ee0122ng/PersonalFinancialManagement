import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { MaterialModule } from 'src/material.module';
import { ActivateComponent } from './Components/activate.component';
import { HomeComponent } from './Components/home.component';
import { LoginComponent } from './Components/login.component';
import { ProfileComponent } from './Components/profile.component';
import { RegisterComponent } from './Components/register.component';
import { AuthoriseAccountService } from './Services/authorise-account.service';
import { LoginAccountService } from './Services/login-account.service';
import { RegisterAccountService } from './Services/register-account.service';
import { UserProfileService } from './Services/user-profile.service';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LogoutAccountService } from './Services/logout-account.service';
import { UserExpensesService } from './Services/user-expenses.service';
import { UploadProfilePictureService } from './Services/upload-profile-picture.service';
import { ExpenseTransactionComponent } from './Components/expense-transaction.component';
import { ExpenseRecordComponent } from './Components/expense-record.component';
import { ExpenseSummaryComponent } from './Components/expense-summary.component';
import { AuthInterceptorService } from './Services/auth-interceptor.service';
import { GoogleapiCallbackComponent } from './Components/googleapi-callback.component';

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    LoginComponent,
    ActivateComponent,
    HomeComponent,
    ProfileComponent,
    ExpenseTransactionComponent,
    ExpenseRecordComponent,
    ExpenseSummaryComponent,
    GoogleapiCallbackComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    ReactiveFormsModule,
    MaterialModule,
    HttpClientModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    })
  ],
  providers: [ 
    RegisterAccountService,
    UserProfileService,
    LoginAccountService,
    AuthoriseAccountService,
    LogoutAccountService,
    UserExpensesService,
    UploadProfilePictureService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptorService,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
