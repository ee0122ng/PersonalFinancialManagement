import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RegisterAccountService } from '../Services/register-account.service';
import { UserProfileService } from '../Services/user-profile.service';
import { AppComponent } from '../app.component';
import { Country } from '../models';

@Component({
  selector: 'app-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.css']
})
export class ActivateComponent implements OnInit, AfterViewChecked {

  accountId !: string;
  userEmail : string = "";
  activateForm !: FormGroup;
  countries : Country[] = [];
  selectedCountry !: string;
  countryImageUrl !: string;
  userInfoCompleted: Boolean = false;
  updateError !: string;

  constructor(private activatedRoute: ActivatedRoute, private router: Router, private fb: FormBuilder, private activateAccSvc: UserProfileService, private registerAccSvc: RegisterAccountService) {}

  ngOnInit(): void {

    if (!!localStorage.getItem("loginStatus")) {
      //@ts-ignore
      let userData = JSON.parse(localStorage.getItem("loginStatus"))
      this.accountId = userData.accountId
      this.userEmail = userData.email
    }

    this.activateForm = this.createForm()
    this.activateAccSvc.getCountriesList()      
      .then( (p: any[]) => {
        return p.map (
            (c: any) => {
              return {
              name: c["name"]["common"],
              flag: c["flags"]["png"]
            } as Country
          })
      })
      .then( (l: Country[]) => {
        this.countries = l.sort( (a : Country, b: Country) => a.name.localeCompare(b.name) )
      })
  }

  ngAfterViewChecked(): void {
    
  }

  createForm() : FormGroup {
    return this.fb.group({
      firstname: this.fb.control<string>('', [Validators.required]),
      lastname: this.fb.control<string>(''),
      email: this.fb.control<string>(this.userEmail, [Validators.required, Validators.email]),
      dob: this.fb.control<Date>(new Date()),
      occupation: this.fb.control<string>(''),
      country: this.fb.control<string>('')
    })
  }

  selectCountry() {
    this.selectedCountry = this.activateForm.get("country")?.value
    if (this.selectedCountry) {
      this.countries.filter( c => c.name == this.selectedCountry).map( c => this.countryImageUrl = c.flag)
    }
  }

  completeAccount() {
    this.activateAccSvc.updateUserInfo(this.activateForm, this.accountId)
      .then( (p:any) => {
        console.info(">>> account completion: " + p["payload"])
        this.userInfoCompleted = true
        AppComponent.infoCompletionStatus.next(this.userInfoCompleted)
        this.router.navigate(['/home']);
      })
      .catch( (err:any) => {
        console.info(">>> account completion error: " + err["error"]["error"])
        this.updateError = err["error"]["error"]
      })
  }

  routeToHome() {
    this.activateForm.reset()
    this.router.navigate(['/home'])
  }

}
