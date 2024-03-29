import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthoriseAccountService } from './Services/authorise-account.service';
import { LogoutAccountService } from './Services/logout-account.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit ,OnDestroy {
  title = 'pfm_expenses_client';

  public static loginStatus : Subject<Boolean> = new Subject();
  public static infoCompletionStatus : Subject<Boolean> = new Subject();
  public static currentAccountId : Subject<string> = new Subject();
  public static currentUserEmail : Subject<string> = new Subject();
  public static currentUsername : Subject<string> = new Subject();

  loginSuccess : Boolean = false;
  accountCompleted : Boolean = false;
  accountId : string = "";
  userEmail : string = "";
  username : string = "";

  constructor(private router: Router, private logoutAccountService: LogoutAccountService, private sessionCheckSvc: AuthoriseAccountService) {
    AppComponent.loginStatus.subscribe( (s:any) => { this.loginSuccess = s })
    AppComponent.infoCompletionStatus.subscribe( (a:any) => { this.accountCompleted = a})
    AppComponent.currentAccountId.subscribe( (c:any) => { this.accountId = c })
    AppComponent.currentUserEmail.subscribe( (e:any) => { this.userEmail = e })
    AppComponent.currentUsername.subscribe( (u:any) => {this.username = u })
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

      // this.sessionCheckSvc.checkSession()
      //   .then(
      //     (p:any) => {
      //       AppComponent.currentUsername.next(p["username"])
      //       this.router.navigate(['/summary'])
      //     }
      //   )
      //   .catch(
      //     (err:any) => {
      //       console.info(JSON.stringify(err))
      //     }
      //   )
      
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
    this.loginSuccess = false;
    this.accountCompleted = false;
    this.accountId = "";
    this.userEmail = "";
    this.logoutAccountService.logout();
  }

}
