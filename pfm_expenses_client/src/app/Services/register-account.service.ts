import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { RegisterForm } from '../models';
import { lastValueFrom } from 'rxjs';
import { RAILWAY_DOMAIN, REGISTER_API_URL } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class RegisterAccountService {

  constructor(private http: HttpClient) { }

  public registerAccount(form: FormGroup) : Promise<any> {

    const registerForm : RegisterForm = {
      username: form.get("username")?.value,
      password: form.get("password")?.value,
      email: form.get("email")?.value,
    }

    return lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+REGISTER_API_URL, registerForm));
    // return lastValueFrom(this.http.post<any>(REGISTER_API_URL, registerForm));

  }

}
