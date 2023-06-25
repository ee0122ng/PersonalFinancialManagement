import { Injectable } from '@angular/core';
import { CLIENT_ID, GOOGLEAPICALL_SERVER_API_URL, GOOGLE_API_SCOPES, GOOGLE_GET_CALENDAR_ENDPOINT, GOOGLE_POST_EVENT_ENDPOINT, RAILWAY_DOMAIN, REDIRECT_URL } from '../constants';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { GoogleAuthToken, GoogleEvent } from '../models';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class GoogleApiService {

  constructor(private http: HttpClient, private router: Router) { }

  // get calendar list
  public getCalendar() : Promise<any> {
    const headers = new HttpHeaders()
                        .set('Content-Type', 'application/json; charset=utf-8')
                        .set('Authorization', 'Bearer '+this.retrieveToken())
    return lastValueFrom(this.http.get<any>(GOOGLE_GET_CALENDAR_ENDPOINT, { headers }));
  }

  // insert event 
  public postEvent(event: GoogleEvent) : Promise<any> {
    const headers = new HttpHeaders()
                        .set('Content-Type', 'application/json; charset=utf-8')
                        .set('Authorization', 'Bearer '+this.retrieveToken())
    return lastValueFrom(this.http.post<any>(GOOGLE_POST_EVENT_ENDPOINT, event, { headers }))
  }

  // perform google oauth2 login through server
  private invokeServerForGoogleToken() {
    lastValueFrom(this.http.get<any>(RAILWAY_DOMAIN+GOOGLEAPICALL_SERVER_API_URL))
    // lastValueFrom(this.http.get<any>(GOOGLEAPICALL_SERVER_API_URL))
      .then(
        (p:any) => {
          const ex_date : Date = new Date()
          ex_date.setSeconds(ex_date.getSeconds() + p["expires_in"])
          const gAuthToken : GoogleAuthToken = {
            token: p["token"],
            expires_in: p["expires_in"],
            expires_date: ex_date
          }
          sessionStorage.setItem("googleToken", JSON.stringify(gAuthToken));
          this.router.navigate(['/transaction'])
        }
      )
      .catch (
        (err:any) => {
          console.info(">>> error: " + JSON.stringify(err))
        }
      )
  }

  // perform google oauth2 login
  public invokeGoogleApi() {
    this.oauthSignIn()
  }

  // create a form to invoke oauth signin
  // note: Google Api does not support CORS
  protected oauthSignIn() {
    // Google's OAuth 2.0 endpoint for requesting an access token
    var oauth2Endpoint = 'https://accounts.google.com/o/oauth2/v2/auth';

    // Create <form> element to submit parameters to OAuth 2.0 endpoint.
    var form = document.createElement('form');
    form.setAttribute('method', 'GET'); // Send as a GET request.
    form.setAttribute('action', oauth2Endpoint);

    // Parameters to pass to OAuth 2.0 endpoint.
    var params : {[key:string] : any} = {'client_id': CLIENT_ID,
                  'redirect_uri': REDIRECT_URL,
                  'response_type': 'token',
                  'scope': GOOGLE_API_SCOPES,
                  'include_granted_scopes': 'true'};
    

    // Add form parameters as hidden input values.
    for (var p in params) {
      var input = document.createElement('input');
      input.setAttribute('type', 'hidden');
      input.setAttribute('name', p);
      
      input.setAttribute('value', params[p])
      form.appendChild(input);
    }

    // Add form to page and submit it to open the OAuth 2.0 endpoint.
    document.body.appendChild(form);
    form.submit();
  }

  protected retrieveToken() : string {
    let storage = sessionStorage.getItem('gToken')

    if (!!storage) {
      const token = JSON.parse(storage).token
      return token
    }
    return "";
  }
}
