import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GoogleAuthToken } from '../models';

@Component({
  selector: 'app-googleapi-callback',
  templateUrl: './googleapi-callback.component.html',
  styleUrls: ['./googleapi-callback.component.css']
})
export class GoogleapiCallbackComponent {

  private token : string = ""
  private expires_in : number = 0

  constructor(private activatedRoute: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.activatedRoute.fragment
      .subscribe(
        (f:any) => {
          const v : string[] = f.split("&")
          this.token = v[0].split("=")[1]
          this.expires_in = +v[2].split("=")[1]

          const ex_date : Date = new Date()
          ex_date.setSeconds(ex_date.getSeconds() + this.expires_in)
          const gAuthToken : GoogleAuthToken = {
            token: this.token,
            expires_in: this.expires_in,
            expires_date: ex_date
          }
          sessionStorage.setItem("googleToken", JSON.stringify(gAuthToken));
          this.router.navigate(['/transaction'])
        }
      )
      .unsubscribe()

  }

}
