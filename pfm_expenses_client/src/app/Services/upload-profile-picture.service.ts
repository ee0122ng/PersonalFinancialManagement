import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { PROFILEPICUPLOAD_API_URL } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class UploadProfilePictureService {

  profilePicUrl : string = '';
  $profilePicProm !: Promise<any>;

  constructor(private http: HttpClient) { }

  uploadProfilePicture(formData : FormData) : Promise<any> {

    this.$profilePicProm = lastValueFrom(this.http.post<any>(PROFILEPICUPLOAD_API_URL, formData))

    this.$profilePicProm
      .then(
        (p:any) => {
          this.profilePicUrl = p["endpoint"]
        }
      )

    return this.$profilePicProm
  }

}