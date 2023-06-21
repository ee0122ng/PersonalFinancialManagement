import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor {

  constructor(private router: Router) { }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // let session = sessionStorage.getItem("googleToken");
    // // inject googleapi token to header
    // if (!!session) {
    //   // check if token still valid
    //   //@ts-ignore
    //   if (JSON.parse(session).dateTime <= new Date()) {
    //     this.router.navigate(['/something']) // insert later
    //   }
    //   //@ts-ignore
    //   let gToken = JSON.parse(session).token
    //   if (!!gToken) {
    //     const r = req.clone({
    //       setHeaders: {
    //         Authorization: `Bearer ${gToken}`
    //       }
    //     })
    //     return next.handle(r)
    //   }
    // }

    let storageData = localStorage.getItem("loginStatus");
    // inject Jwt token to header
    if (!!storageData) {
      //@ts-ignore
      let token = JSON.parse(storageData).jwt
      if (!!token) {
        const validReq = req.clone({
          setHeaders: {
            JwtAuth: `Bearer ${token}`,
          }
        })
        return next.handle(validReq)
      }
    }

    return next.handle(req)

  }
}
