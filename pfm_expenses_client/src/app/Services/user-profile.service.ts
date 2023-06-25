import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { lastValueFrom } from 'rxjs';
import { COUNTRY_API_URL, PROFILE_API_URL, RAILWAY_DOMAIN } from '../constants';
import { Country, UserDetails } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {

  countryList !: Country[];
  $countryProm !: Promise<any>;

  constructor(private http: HttpClient) { }

  getCountriesList() : Promise<any> {
    this.$countryProm = lastValueFrom(this.http.get<any>(COUNTRY_API_URL))

    return this.$countryProm
  }

  updateUserInfo(form: FormGroup, accId: string) : Promise<any> {

    const userDetails : UserDetails = {
      accountId: accId,
      email: form.get('email')?.value,
      firstname: form.get('firstname')?.value,
      lastname: form.get('lastname')?.value,
      dob: form.get('dob')?.value,
      country: form.get('country')?.value,
      occupation: form.get('occupation')?.value
    }

    return lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+PROFILE_API_URL, userDetails))
    // return lastValueFrom(this.http.put<any>(PROFILE_API_URL, userDetails))

  }

  createUserInfo(form: FormGroup, accId: string) : Promise<any> {

    const userDetails : UserDetails = {
      accountId: accId,
      email: form.get('email')?.value,
      firstname: form.get('firstname')?.value,
      lastname: form.get('lastname')?.value,
      dob: form.get('dob')?.value,
      country: form.get('country')?.value,
      occupation: form.get('occupation')?.value
    }
    return lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+PROFILE_API_URL, userDetails))
    // return lastValueFrom(this.http.post<any>(PROFILE_API_URL, userDetails))

  }

  retrieveUserProfile(username: string) : Promise<any> {

    const params : HttpParams = new HttpParams().set("username", username) 

    return lastValueFrom(this.http.get<any>(RAILWAY_DOMAIN+PROFILE_API_URL, { params }))
    // return lastValueFrom(this.http.get<any>(PROFILE_API_URL, { params }))

  }
  

}
