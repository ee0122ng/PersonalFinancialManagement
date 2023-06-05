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
  countries : Country[] = [];
  selectedCountry !: string;
  countryImageUrl !: string;

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
                          this.countries = l.sort( (a : Country, b: Country) => a.name.localeCompare(b.name) )
                        })
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

  selectCountry() {
    this.selectedCountry = this.activateForm.get("country")?.value
    console.info(">>> sample flag: " + this.selectedCountry)
    if (this.selectedCountry) {
      this.countries.filter( c => c.name == this.selectedCountry).map( c => this.countryImageUrl = c.flag)
      console.info(">>> sample flag: " + this.countryImageUrl)
    }
  }

}
