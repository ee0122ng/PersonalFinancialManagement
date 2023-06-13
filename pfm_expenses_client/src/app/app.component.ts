import { AfterContentInit, AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { LoginComponent } from './Components/login.component';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';
import { LogoutAccountService } from './Services/logout-account.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit ,OnDestroy {
  title = 'pfm_expenses_client';

  public static loginStatus : Subject<Boolean> = new Subject();
  public static infoCompletionStatus : Subject<Boolean> = new Subject();
  public static currentAccountId : Subject<String> = new Subject();
  public static currentUserEmail : Subject<String> = new Subject();
  public static currentUsername : Subject<String> = new Subject();

  loginSuccess : Boolean = false;
  accountCompleted !: Boolean | undefined;
  accountId !: string | undefined;
  userEmail !: string | undefined;

  constructor(private router: Router, private logoutAccountService: LogoutAccountService) {
    AppComponent.loginStatus.subscribe( (s:any) => { this.loginSuccess = s })
    AppComponent.infoCompletionStatus.subscribe( (a:any) => { this.accountCompleted = a})
    AppComponent.currentAccountId.subscribe( (c:any) => { this.accountId = c })
    AppComponent.currentUserEmail.subscribe( (e:any) => { this.userEmail = e })
  }

  ngOnInit(): void {

    // check cached user data at local storage
    if(!!localStorage.getItem("loginStatus")) {

      //@ts-ignore
      let persistDetails = JSON.parse(localStorage.getItem('loginStatus'))

      AppComponent.loginStatus.next(persistDetails.loginStatus)
      AppComponent.infoCompletionStatus.next(persistDetails.profileCompStatus)
      AppComponent.currentAccountId.next(persistDetails.accountId)
      AppComponent.currentUserEmail.next(persistDetails.email)
      AppComponent.currentUsername.next(persistDetails.username)
    }

  }

  ngOnDestroy(): void {
    AppComponent.loginStatus.unsubscribe()
    AppComponent.infoCompletionStatus.unsubscribe()
    AppComponent.currentAccountId.unsubscribe()
    AppComponent.currentUserEmail.unsubscribe()
    AppComponent.currentUsername.unsubscribe()
  }

  logout() {
    localStorage.clear()
    this.loginSuccess = false;
    this.accountCompleted = undefined;
    this.accountId = undefined;
    this.userEmail = undefined;
    this.logoutAccountService.logout();
    this.router.navigate(['/login'])
  }

}
