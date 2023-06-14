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

export interface Profile {
    firstname: string;
    lastname: string;
    email: string;
    dob: Date;
    age: number;
    country: string;
    occupation: string;
    imageUrl: string;
}

export interface PersistDetails {
    username : string | null;
    accountId : string | null;
    email : string | null;
    profileCompStatus : Boolean | null;
    loginStatus : Boolean | null;
}

export interface Transaction {
    id: number;
    userId: number;
    category: string;
    item: string;
    amount: number;
    transactionDate: Date;
    currency: string;
}