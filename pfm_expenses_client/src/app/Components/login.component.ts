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
  accountId !: string | any;
  loginStatus : Boolean = false
  errorMessage !: string

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

        // update the parent status
        AppComponent.loginStatus.next(this.loginStatus)
        
        this.router.navigate(['/home'])
      })
      .catch( (e:any) => {
        console.info(">>> error: " + e["error"]["error"])
        this.errorMessage = e["error"]["error"]
      })

    }
  }

}
