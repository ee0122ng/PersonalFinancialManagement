import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserProfileService } from '../Services/user-profile.service';
import { Profile } from '../models';
import { Country } from '../models';
import { UploadProfilePictureService } from '../Services/upload-profile-picture.service';

@Component({
  selector: 'app-retrieve-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  @ViewChild('profilePic')
  profileImage !: ElementRef;

  profile !: Profile
  username : string = ""
  flag : string = ""
  accountId : string = ""
  imageUploaded : Boolean = false;
  profilePicUrl : string = ""
  errorMessage : string = ""

  constructor(private userProfileSvc : UserProfileService, private activatedRoute: ActivatedRoute, private uploadProfilePicSvc: UploadProfilePictureService) {}

  ngOnInit(): void {

    if (!!localStorage.getItem("loginStatus")) {

      //@ts-ignore
      let userData = JSON.parse(localStorage.getItem("loginStatus"))
      this.username = this.activatedRoute.snapshot.params['username'];
      
      this.accountId = userData.accountId

    }

    this.userProfileSvc.retrieveUserProfile(this.username)
    .then( (p:any) => {
      return {
        firstname: p["firstname"],
        lastname: p["lastname"],
        email: p["email"],
        dob: new Date(p["dob"]),
        age: p["age"],
        country: p["country"],
        occupation: p["occupation"],
        imageUrl: p["profileUrl"]
      } as Profile
    })
    .then((p:Profile) => {

      this.profile = p

      // check if there is latest profile pic uploaded
      this.profilePicUrl = p.imageUrl

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
                }
              }
            )
          }
        )
      }
    })
    .catch(
      (err:any) => {
        console.info(">>> error: " + JSON.stringify(err))
      }
    )
  }

  async onFileUpload($event: any) {
    let file = $event.target.files[0]

    // upload image to bucket if there is file attached
    if (file) {
      const formData : FormData = new FormData();
      formData.set("profilePic", this.profileImage.nativeElement.files[0])
      formData.set("username", this.username)
      formData.set("accountId", this.accountId)

      this.uploadProfilePicSvc.uploadProfilePicture(formData)
        .then(
          (p:any) => {
            this.imageUploaded = true

            // append a query text to act as image source refresh
            this.profilePicUrl = `${p["endpoint"]}?time=${Date.now()}`

          }
        )
        .catch(
          (err:any) => {
            console.info(">>> error: " + JSON.stringify(err))
            this.imageUploaded = false
            this.errorMessage = err["error"]["error"]
          }
        )
      
    }
  }

}
