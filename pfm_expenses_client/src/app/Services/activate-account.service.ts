import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { COMPLETE_API_URL, COUNTRY_API_URL } from '../constants';
import { FormGroup } from '@angular/forms';
import { UserDetails } from '../models';

@Injectable({
  providedIn: 'root'
})
export class ActivateAccountService {

  constructor(private http: HttpClient) { }

  getCountriesList() : Promise<any> {
    return lastValueFrom(this.http.get<any>(COUNTRY_API_URL))
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

    console.info(">>> user details: " + JSON.stringify(userDetails));

    return lastValueFrom(this.http.post<any>(COMPLETE_API_URL, userDetails))

  }
}
