### Personal Financial Management Application ###

### For Login Function
1. This application will authenticate the user based their login status
2. Specific functions will only be rendered given current user is authenticated
3. For non-authenticated user, summary of current month expenses will be displayed

#### For CRUD Functions (Authenticated)
1. Scheduled Fees Deduction, Auto Investment Deduction (enable recursion link to Google Calendar), Auto Monthly Salary Insertion (enable recursion)
2. Mortgage / Loan Installment

#### For Expense Tracking only (Authenticated - without 2FA)
1. Remember by user session

### For Personalised Functions (Authenticated - with 2FA)
1. Daily, Weekly, Monthly, Yearly Summary
2. Personal Financial Goals (Bot Advice + Notification to enable bot insert function)
3. Add source of income, investment, protection details
4. Add payment gateway function (for setting up auto-payment like giro)
5. Add security layer for account deletion to prevent sabotage

### For storage
1. Mongo: records db: to store statement or summary
2. MySQL: finance db, account: to store hashed pwd and salt
3. MySQL: finance db, user: to store user details
3. MySQL: finance db, activity: to store

### To create self-signed TSL/SSL (Transport Security Layer or Secure Socket Layer)
1. Create a Certificate Authority (CA) and generate a root CA private key
2. Generate server private key
3. Create CSR with the server private key
4. Generate SSL with the CSR using root CA and root CA private key
5. Install CA in the browser



