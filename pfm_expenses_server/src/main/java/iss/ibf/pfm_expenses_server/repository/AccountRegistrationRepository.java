package iss.ibf.pfm_expenses_server.repository;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import iss.ibf.pfm_expenses_server.exception.AccountCreationException;
import iss.ibf.pfm_expenses_server.exception.UsernameException;
import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.User;

@Repository
public class AccountRegistrationRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final String VERIFY_USERNAME_SQL = "select * from users where username=?";
    private final String CREATE_NEW_USER_SQL = "insert into users(user_id, username) values(?, ?)";
    private final String CREATE_NEW_ACCOUNT_SQL = "insert into accounts(account_id, user_id, salt_string, hashed_string, is_valid) values(?, ?, ?, ?, ?)";
    private final String CREATE_NEW_USER_DETAILS_SQL = "insert into user_details(user_id, email) values(?, ?)";

    @ Transactional
    public String createUserAccount(User user, String pwd, String email) {

        Boolean usernameExists = verifyIfUsernameExist(user.getUsername());
        Boolean newUserCreated = false;
        Boolean newAccountCreated = false;
        Boolean newUserInfoCreated = false;

        if (!usernameExists) {

            // create user entity
            newUserCreated = jdbcTemplate.update(CREATE_NEW_USER_SQL, user.getUserId(), user.getUsername()) > 0;

            // generate hash string
            String[] result = generateHashString(pwd);
            String saltString = result[1];
            String hashString = result[0];

            // create user account
            Account account = new Account(user.getUserId());
            newAccountCreated = jdbcTemplate.update(CREATE_NEW_ACCOUNT_SQL, account.getAccountId(), user.getUserId(), saltString, hashString, account.getIsValid()) > 0;

            // insert new user details
            newUserInfoCreated = jdbcTemplate.update(CREATE_NEW_USER_DETAILS_SQL, user.getUserId(), email) > 0;

            if (newUserCreated && newAccountCreated && newUserInfoCreated) {

                return account.getAccountId();

            } else {

                throw new AccountCreationException("Registration failed");
            }

        } else {

            throw new UsernameException("Username in use");
        }

    }

    public Boolean verifyIfUsernameExist(String username) {

        // get all existing username
        List<User> users = jdbcTemplate.query(VERIFY_USERNAME_SQL, BeanPropertyRowMapper.newInstance(User.class) ,username);
        List<String> usernames = users.stream().map(u -> u.getUsername()).toList();

        return usernames.contains(username);
    }

    public String[] generateHashString(String pwd) {

        // generate salt
        String salt = BCrypt.gensalt(14, new SecureRandom());

        // generate hash
        String hash = BCrypt.hashpw(pwd, salt);

        // return both salt and hash
        String[] arr = {hash, salt};

        return arr;
    }
    
}
