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
  loginSuccess : Boolean = false;

  constructor(private router: Router) {
    AppComponent.loginStatus.subscribe( (s:any) => { this.loginSuccess = s })
  }

  logout() {
    AppComponent.loginStatus.unsubscribe()
    this.loginSuccess = false;
    this.router.navigate(['/login'])
  }

  ngOnDestroy(): void {
      AppComponent.loginStatus.unsubscribe()
  }

}
