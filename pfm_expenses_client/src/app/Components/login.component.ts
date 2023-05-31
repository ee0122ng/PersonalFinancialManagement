import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm !: FormGroup;
  accountId !: string | any;

  constructor(private fb: FormBuilder, private router: ActivatedRoute, private activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
      this.loginForm = this.createFrom()
  }

  createFrom() : FormGroup {

    return this.fb.group({
      username: this.fb.control<string>('', [Validators.required]),
      password: this.fb.control<string>('')
    })
  }

  login() {
    
  }

}
