import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialModule } from 'src/material.module';
import { HttpClientModule } from '@angular/common/http';
import { ServiceWorkerModule } from '@angular/service-worker';
import { RegisterComponent } from './Components/register.component';
import { LoginComponent } from './Components/login.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RegisterAccountService } from './Services/register-account.service';
import { ActivateComponent } from './Components/activate.component';
import { ActivateAccountService } from './Services/activate-account.service';
import { HomeComponent } from './Components/home.component';
import { LoginAccountService } from './Services/login-account.service';

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    LoginComponent,
    ActivateComponent,
    HomeComponent
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
    ActivateAccountService,
    LoginAccountService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
