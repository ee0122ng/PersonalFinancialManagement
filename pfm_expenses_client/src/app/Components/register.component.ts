import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { RegisterAccountService } from '../Services/register-account.service';
import { AppComponent } from '../app.component';
import { UserData } from '../models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  registerForm !: FormGroup;
  accId : string = ""
  failedMessage : string = ""
  hide : Boolean = true;
  hideConfirm : Boolean = true;

  constructor(private fb: FormBuilder, private router: Router, private regAccSvc: RegisterAccountService) {}

  ngOnInit(): void {
    this.registerForm = this.createForm()
  }

  createForm() : FormGroup {

    return this.fb.group({
      username: this.fb.control<string>('', [Validators.required, Validators.min(3)]),
      password: this.fb.control<string>('', [Validators.required]),
      confirmedPwd: this.fb.control<string>('', [Validators.required]),
      email: this.fb.control<string>('', [Validators.required, Validators.email])
    })
  }

  isFormValid() : Boolean {

    const isPwdMatch : Boolean = (this.registerForm.get("password")?.value === this.registerForm.get("confirmedPwd")?.value)

    return this.registerForm.valid && isPwdMatch
  }

  registerAccount() {
    this.regAccSvc.registerAccount(this.registerForm)
      .then( (p: any) => {
        this.accId = p["accountId"]
        AppComponent.currentAccountId.next(this.accId)
        AppComponent.currentUserEmail.next(this.registerForm.get('email')?.value)
        
        // cache user data to local storage
        const userData : UserData = {
          accountId: this.accId,
          email: this.registerForm.get('email')?.value
        } as UserData
        localStorage.setItem("userData", JSON.stringify(userData));

        this.router.navigate(['/activate', this.accId])
      })
      .catch( (e:any) => {
        console.info(">>> error: " + JSON.stringify(e));
        this.failedMessage = e["error"]["error"]
      })
  }

}
