import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { PROFILEPICUPLOAD_API_URL, RAILWAY_DOMAIN } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class UploadProfilePictureService {

  $profilePicProm !: Promise<any>;

  constructor(private http: HttpClient) { }

  uploadProfilePicture(formData : FormData) : Promise<any> {

    this.$profilePicProm = lastValueFrom(this.http.post<any>(RAILWAY_DOMAIN+PROFILEPICUPLOAD_API_URL, formData))
    // this.$profilePicProm = lastValueFrom(this.http.post<any>(PROFILEPICUPLOAD_API_URL, formData))

    return this.$profilePicProm
  }

}
