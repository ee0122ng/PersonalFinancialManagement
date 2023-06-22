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
    jwt: string;
    accountId : string | null;
    email : string | null;
    profileCompStatus : Boolean | null;
    loginStatus : Boolean | null;
}

export interface TransactionRecord {
    id: number;
    category: string;
    item: string;
    transactionDate: Date;
    amount: number;
    currency: string;
}

export interface RecordTable {
    id: number;
    position: number;
    category: string;
    item: string;
    transactionDate: Date;
    amount: number;
    currency: string;
}

export interface GoogleAuthToken {
    token : string;
    expires_in: number;
    expires_date: Date;
}

export interface StartEvent {
    dateTime: Date;
    timeZone: string;
}

export interface EndEvent {
    dateTime: Date;
    timeZone: string;
}

export interface RecurrenceEvent {
    recurrence: string[];
}

export interface EventTable {
    frequency: string;
    count: number;
    summary: string;
    description: string;
    email: string;
}

export interface GoogleEvent {
    frequency: string;
    count: number;
    summary: string;
    description: string;
    email: string;
    startDate: string;
}


export class Transaction {
    id !: number;
    userId !: string;
    category !: string;
    item !: string;
    amount !: number;
    transactionDate !: Date;
    currency !: string;
    
    constructor() {}
}

export class GoogleEvent {
    summary !: string;
    description !: string;
    start !: StartEvent;
    end !: EndEvent;
    recurrence !: RecurrenceEvent;
}