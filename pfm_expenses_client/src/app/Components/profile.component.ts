import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { filter, lastValueFrom, map } from 'rxjs';
import { LoginAccountService } from '../Services/login-account.service';
import { UserProfileService } from '../Services/user-profile.service';
import { COUNTRY_API_URL } from '../constants';
import { Profile } from '../models';
import { Country } from '../models';

@Component({
  selector: 'app-retrieve-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  profile !: Profile
  username !: string;
  flag !: string;
  accountId !: string;

  constructor(private userProfileSvc : UserProfileService, private loginAccSvc: LoginAccountService ,private router : Router, private http: HttpClient) {}

  ngOnInit(): void {
    this.username = this.loginAccSvc.getUsername()
    this.accountId = this.loginAccSvc.getAccountId()

    this.userProfileSvc.retrieveUserProfile(this.username)
    .then( (p:any) => {
      return {
        firstname: p["firstname"],
        lastname: p["lastname"],
        email: p["email"],
        dob: new Date(p["dob"]),
        age: p["age"],
        country: p["country"],
        occupation: p["occupation"]
      } as Profile
    })
    .then((p:Profile) => {
      this.profile = p
      console.info(">>> sample retrieval: " + !!this.profile.country)

      // if country name is not blank, get the flag url
      if (!!this.profile.country) {
       //TODO: filter the list to get flag png
       this.userProfileSvc.getCountriesList()
        .then(
          (p:any[]) => {
            return p.map(
              (c:any) => {
                return {
                  name: c["name"]["common"],
                  flag: c["flags"]["png"]
                } as Country
              }
            )
          }
        )
        .then(
          (cl:Country[]) => {
            cl.find(
              (c:Country) => {
                if(c.name === this.profile.country) {
                  this.flag = c.flag
                  console.info(">>> sample flag: " + this.flag)
                }
              }
            )
          }
        )
      }
    })
  }

}
