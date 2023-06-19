import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor {

  constructor() { }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

      let storageData = localStorage.getItem("loginStatus");

      if (!!storageData) {
        //@ts-ignore
        let token = JSON.parse(storageData).jwt
        if (!!token) {
          const validReq = req.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`,
            }
          })
          console.info(">>> valid request: " + JSON.stringify(validReq))
          return next.handle(validReq)
        }
      }

      return next.handle(req)

  }
}
