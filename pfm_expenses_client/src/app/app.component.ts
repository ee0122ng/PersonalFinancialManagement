import { AfterContentInit, AfterViewInit, Component, OnDestroy, ViewChild } from '@angular/core';
import { LoginComponent } from './Components/login.component';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnDestroy {
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

  constructor(private router: Router) {
    AppComponent.loginStatus.subscribe( (s:any) => { this.loginSuccess = s })
    AppComponent.infoCompletionStatus.subscribe( (a:any) => { this.accountCompleted = a})
    AppComponent.currentAccountId.subscribe( (c:any) => { this.accountId = c })
    AppComponent.currentUserEmail.subscribe( (e:any) => { this.userEmail = e })
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
    this.accountCompleted = undefined;
    this.accountId = undefined;
    this.userEmail = undefined;
    this.router.navigate(['/login'])
  }

}
