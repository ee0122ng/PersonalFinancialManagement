import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LOGOUT_API_URL } from '../constants';
import { lastValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LogoutAccountService {

  message: string = '';

  constructor(private http: HttpClient) { }

  logout() {
    lastValueFrom(this.http.get<any>(LOGOUT_API_URL))
      .then(
        (p:any) => {
          this.message = p["payload"]
          console.info(">>> front: " + this.message)
        }
      )
      .catch(
        (err:any) => {
          this.message = err["error"]
        }
      )
  }

  getLogoutMessage() : string {
    return this.message
  }
  
}
