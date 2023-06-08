import { Injectable } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AccountCredential } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthoriseAccountService {

  constructor() { }

  canActivate(username: string, accId: string): boolean {
    return true;
  }

}
