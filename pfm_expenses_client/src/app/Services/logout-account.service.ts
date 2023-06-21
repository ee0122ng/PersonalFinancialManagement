import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { LOGOUT_API_URL } from '../constants';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class LogoutAccountService {

  message: string = '';

  constructor(private http: HttpClient, private router: Router) { }

  logout() {
    const params : HttpParams = new HttpParams();
    lastValueFrom(this.http.post<any>(LOGOUT_API_URL, { params }))
      .then(
        (p:any) => {
          this.message = p["payload"]
          localStorage.clear();
          console.info(">>> front: " + this.message)
          this.router.navigate(['']);
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
