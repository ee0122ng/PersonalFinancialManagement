import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { COMPLETE_API_URL, LOGIN_API_URL } from '../constants';
import { AccountCredential, RegisterForm } from '../models';

@Injectable({
  providedIn: 'root'
})
export class LoginAccountService {

  username !: string;

  constructor(private http: HttpClient) { }

  loginAccount(form: FormGroup) : Promise<any> {

    this.username = form.get('username')?.value

    const credential : AccountCredential = {
      username: form.get("username")?.value,
      password: form.get("password")?.value
    }

    return lastValueFrom(this.http.post<any>(LOGIN_API_URL, credential))
  }

  getUsername() : string {
    return this.username
  }

}
