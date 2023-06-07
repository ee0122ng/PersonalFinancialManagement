export interface RegisterForm {
    username: string;
    password: string;
    email: string;
}

export interface Country {
    name: string;
    flag: string;
}

export interface AccountCredential {
    username: string;
    password: string;
}

export interface UserDetails {
    accountId: string;
    email: string;
    firstname: string;
    lastname: string;
    dob: string;
    country: string;
    occupation: string;
}