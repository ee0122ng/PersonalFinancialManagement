import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { LOGIN_API_URL } from '../constants';
import { AccountCredential, RegisterForm } from '../models';

@Injectable({
  providedIn: 'root'
})
export class LoginAccountService {

  constructor(private http: HttpClient) { }

  loginAccount(form: FormGroup) : Promise<any> {

    const credential : AccountCredential = {
      username: form.get("username")?.value,
      password: form.get("password")?.value
    }

    return lastValueFrom(this.http.post<any>(LOGIN_API_URL, credential))
  }
}
