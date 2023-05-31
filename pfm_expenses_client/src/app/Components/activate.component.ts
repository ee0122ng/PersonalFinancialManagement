import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ActivateAccountService } from '../Services/activate-account.service';
import { Country } from '../models';
import { COUNTRY_API_URL } from '../constants';

@Component({
  selector: 'app-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.css']
})
export class ActivateComponent implements OnInit {

  accountId !: string;
  activateForm !: FormGroup;
  countries : Country[] = []

  constructor(private activatedRoute: ActivatedRoute, private fb: FormBuilder, private activateAccSvc: ActivateAccountService) {}

  ngOnInit(): void {
     this.accountId = this.activatedRoute.snapshot.params['accountId']
     this.activateForm = this.createForm()

     this.activateAccSvc.getCountriesList()
                        .then( (p: any[]) => {
                          return p.map (
                              (c: any) => {
                                return {
                                name: c["name"]["common"],
                                flag: c["flags"]["png"]
                              } as Country
                            })
                        })
                        .then( (l: Country[]) => {
                          l.map(
                            (c: Country) => {
                              this.countries.push(c)
                            }
                          )
                        })
      
      console.info(">>> sample countries list: " + this.countries.at(0))
  }

  createForm() : FormGroup {
    return this.fb.group({
      firstname: this.fb.control<string>('', [Validators.required]),
      lastname: this.fb.control<string>(''),
      dob: this.fb.control<Date>(new Date()),
      occupation: this.fb.control<string>(''),
      country: this.fb.control<string>('')
    })
  }

}
