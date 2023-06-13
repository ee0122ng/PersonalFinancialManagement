import { Component, OnInit, Output, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthoriseAccountService } from '../Services/authorise-account.service';
import { LoginAccountService } from '../Services/login-account.service';
import { AppComponent } from '../app.component';
import { PersistDetails } from '../models';

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
  username !: string | any;
  hide: Boolean = true;

  @Output()
  onSuccessfulLogin = new Subject<Boolean>();

  constructor(private fb: FormBuilder, private loginSvc: LoginAccountService, private router: Router, private authAccount: AuthoriseAccountService) {}

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
        this.username = this.loginForm.get('username')?.value

        // persist user data to local storage
        const persistDetails : PersistDetails = {
          username: this.username,
          accountId: this.accountId,
          email: this.userEmail,
          profileCompStatus: this.accountCompleted,
          loginStatus: this.loginStatus
        } as PersistDetails

        localStorage.setItem("loginStatus", JSON.stringify(persistDetails))

        // authenticate the user
        const canActivateAccount: CanActivateFn =
        (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
          return inject(AuthoriseAccountService).canActivate(inject(this.username), this.accountId);
        };

        //update the parent status
        AppComponent.loginStatus.next(this.loginStatus)
        AppComponent.infoCompletionStatus.next(this.accountCompleted)
        AppComponent.currentAccountId.next(this.accountId)
        AppComponent.currentUserEmail.next(this.userEmail)
        AppComponent.currentUsername.next(this.username)
        this.router.navigate(['/home'])
      })
      .catch( (e:any) => {
        this.errorMessage = e['error']['error']
      })

    }
  }

  getPersistedDetails(key: string) {
    localStorage.getItem(key)
  }

}
