// external api
export const COUNTRY_API_URL = 'https://restcountries.com/v3.1/all?fields=name,flags';

// account & profile related api
export const REGISTER_API_URL = '/api/account/register';
export const LOGIN_API_URL = '/api/account/login';
export const LOGOUT_API_URL = '/api/account/logout';
export const PROFILE_API_URL = '/api/profile';
export const PROFILEPICUPLOAD_API_URL = '/api/profile/image-upload';

// transaction related api
export const CURRENCY_API_URL = '/api/transaction/currencies';
export const TRANSACTION_INSERT_API_URL = '/api/transaction/insert';
export const TRANSACTION_QUERY_API_URL = '/api/transaction/retrieve';
export const TRANSACTION_UPDATE_API_URL = '/api/transaction/update';
export const TRANSACTION_DELETE_API_URL = '/api/transaction/delete';
export const NOTIFICATION_CREATE_API_URL = '/api/notification/create';
export const TRANSACTION_SUMMARY_API_URL = '/api/transaction/summary';

// google api service
export const GOOGLE_AUTH_ENDPOINT = 'https://accounts.google.com/o/oauth2/v2/auth';
export const GOOGLE_TOKEN_ENDPOINT = 'https://oauth2.googleapis.com/token';
export const REDIRECT_URL = 'http://localhost:4200/callback';
export const GOOGLE_API_SCOPES = 'https://www.googleapis.com/auth/calendar+https://gmail.googleapis.com/upload/gmail/v1/users/me/drafts/send';
export const GOOGLE_EMAIL_SCOPE = 'https://gmail.googleapis.com/upload/gmail/v1/users/me/drafts/send';
export const GOOGLE_CALENDARLIST_SCOPE = 'https://www.googleapis.com/auth/calendar';
export const GOOGLE_GET_CALENDAR_ENDPOINT = 'https://www.googleapis.com/calendar/v3/calendars/primary';
export const GOOGLE_POST_EVENT_ENDPOINT = 'https://www.googleapis.com/calendar/v3/calendars/primary/events';
export const CLIENT_ID = '1084319231188-82t5bgck7e5on0vvtqequ2boag6abb9f.apps.googleusercontent.com';
export const CLIENT_SECRET = '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';
