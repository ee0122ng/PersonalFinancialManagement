import { Injectable } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AccountCredential } from '../models';
import { HttpClient } from '@angular/common/http';
import { VALID_SESSION_API_URL } from '../constants';
import { lastValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthoriseAccountService {

  constructor(private http: HttpClient) { }

  canActivate(username: string, accId: string): boolean {
    return true;
  }

  checkSession() : Promise<any> {
    return lastValueFrom(this.http.get<any>(VALID_SESSION_API_URL));
  }

}
