import { Injectable } from '@angular/core';
import { CLIENT_ID, GOOGLE_API_SCOPES, GOOGLE_CALENDARLIST_SCOPE, GOOGLE_GET_CALENDAR_ENDPOINT, GOOGLE_POST_EVENT_ENDPOINT, REDIRECT_URL } from '../constants';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { GoogleEvent } from '../models';

@Injectable({
  providedIn: 'root'
})
export class GoogleApiService {

  constructor(private http: HttpClient) { }

  // get calendar list
  public getCalendar() : Promise<any> {
    const headers = new HttpHeaders()
                        .set('Content-Type', 'application/json; charset=utf-8')
                        .set('Authorization', 'Bearer '+this.retrieveToken())
    return lastValueFrom(this.http.get<any>(GOOGLE_GET_CALENDAR_ENDPOINT, { headers }));
  }

  // insert event 
  public postEvent(event: GoogleEvent) : Promise<any> {
    // const params : HttpParams = new HttpParams().set("access_token", this.retrieveToken())
    const headers = new HttpHeaders()
                        .set('Content-Type', 'application/json; charset=utf-8')
                        .set('Authorization', 'Bearer '+this.retrieveToken())
    return lastValueFrom(this.http.post<any>(GOOGLE_POST_EVENT_ENDPOINT, event, { headers }))
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
    var params = {'client_id': CLIENT_ID,
                  'redirect_uri': REDIRECT_URL,
                  'response_type': 'token',
                  'scope': GOOGLE_API_SCOPES,
                  'include_granted_scopes': 'true'};
    

    // Add form parameters as hidden input values.
    for (var p in params) {
      var input = document.createElement('input');
      input.setAttribute('type', 'hidden');
      input.setAttribute('name', p);
      //@ts-ignore
      input.setAttribute('value', params[p])
      form.appendChild(input);
    }

    // Add form to page and submit it to open the OAuth 2.0 endpoint.
    document.body.appendChild(form);
    form.submit();
  }

  protected retrieveToken() : string {
    let storage = sessionStorage.getItem('gToken')
    //@ts-ignore
    const token = JSON.parse(storage).token
    return token
  }
}
