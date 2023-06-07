import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { RegisterForm } from '../models';
import { Observable, lastValueFrom } from 'rxjs';
import { REGISTER_API_URL } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class RegisterAccountService {

  email : string = "";

  constructor(private http: HttpClient) { }

  public registerAccount(form: FormGroup) : Promise<any> {

    const registerForm : RegisterForm = {
      username: form.get("username")?.value,
      password: form.get("password")?.value,
      email: form.get("email")?.value,
    }

    this.email = registerForm.email

    return lastValueFrom(this.http.post<any>(REGISTER_API_URL, registerForm));

  }

  public getRegisteredEmail() : string {
    return this.email;
  }

}
