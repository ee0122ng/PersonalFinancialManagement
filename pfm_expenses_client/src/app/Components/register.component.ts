import { Component, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  registerForm !: FormGroup;

  constructor(private fb: FormBuilder) {}

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

}
