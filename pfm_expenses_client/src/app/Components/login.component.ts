import { Component, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { LoginAccountService } from '../Services/login-account.service';
import { AppComponent } from '../app.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm !: FormGroup;
  loginStatus : Boolean = false;
  errorMessage !: string;
  accountCompleted : Boolean = false;
  accountId !: string | any;
  userEmail !: string | any;

  @Output()
  onSuccessfulLogin = new Subject<Boolean>();

  constructor(private fb: FormBuilder, private loginSvc: LoginAccountService, private router: Router) {}

  ngOnInit(): void {
      this.loginForm = this.createFrom()
  }

  createFrom() : FormGroup {

    return this.fb.group({
      username: this.fb.control<string>('', [Validators.required]),
      password: this.fb.control<string>('', [Validators.required])
    })
  }

  login() {
    
    if (this.loginForm.valid) {

      this.loginSvc.loginAccount(this.loginForm)
      .then( (p:any) => {
        this.loginStatus = true
        this.accountCompleted = p["accCompleted"]
        this.accountId = p["accountId"]
        this.userEmail = p["email"]
        //update the parent status
        AppComponent.loginStatus.next(this.loginStatus)
        AppComponent.infoCompletionStatus.next(this.accountCompleted)
        AppComponent.currentAccountId.next(this.accountId)
        AppComponent.currentUserEmail.next(this.userEmail)
        this.router.navigate(['/home'])
      })
      .catch( (e:any) => {
        console.info(">>> error: " + JSON.stringify(e))
        this.errorMessage = e['error']['error']
      })

    }
  }

}
