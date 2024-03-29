import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor {

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    let storageData = localStorage.getItem("loginStatus")
    let sessionData = sessionStorage.getItem("googleToken")

    // inject Jwt token to header
    if (!!storageData && !!sessionData) {
      let token = JSON.parse(storageData).jwt
      let gToken = JSON.parse(sessionData).token
      if (!!token) {
        const validReq = req.clone({
          setHeaders: {
            JwtAuth: `Bearer ${token}`,
            Authorization: `Bearer ${gToken}`
          }
        })
        return next.handle(validReq)
      }
    }

    if (!!storageData) {
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
