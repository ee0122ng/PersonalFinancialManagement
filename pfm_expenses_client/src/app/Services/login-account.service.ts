import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { LOGIN_API_URL } from '../constants';
import { AccountCredential, RegisterForm } from '../models';

@Injectable({
  providedIn: 'root'
})
export class LoginAccountService {

  $loginPromise !: Promise<any>;
  username !: string;
  accountId !: string;

  constructor(private http: HttpClient) { }

  loginAccount(form: FormGroup) : Promise<any> {

    this.username = form.get('username')?.value

    const credential : AccountCredential = {
      username: form.get("username")?.value,
      password: form.get("password")?.value
    }

    let headers : HttpHeaders = new HttpHeaders().set('Access-Control-Allow-Origin', '*')
    this.$loginPromise = lastValueFrom(this.http.post<any>(LOGIN_API_URL, credential, { headers }))
    this.$loginPromise.then((p:any) => {this.accountId = p["accountId"]})

    return this.$loginPromise;
  }

}
