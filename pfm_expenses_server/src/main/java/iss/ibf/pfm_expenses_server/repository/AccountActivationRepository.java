package iss.ibf.pfm_expenses_server.repository;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import iss.ibf.pfm_expenses_server.exception.AccountCreationException;
import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.User;

@Repository
public class AccountActivationRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final String RETURN_ALL_USERNAME = "select username from users";
    private final String CREATE_NEW_USER_SQL = "insert into users(user_id, user_name) values(?, ?)";
    private final String CREATE_NEW_ACCOUNT_SQL = "insert into accounts(account_id, user_id, salt_string, hashed_string, is_valid) values(?, ?, ?, ?, ?)";

    @ Transactional
    public String createUserAccount(User user, String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Boolean usernameExists = verifyIfUsernameExist(user.getUsername());
        Boolean newUserCreated = false;
        Boolean newAccountCreated = false;

        if (!usernameExists) {

            // create user entity
            newUserCreated = jdbcTemplate.update(CREATE_NEW_USER_SQL, user.getUserId(), user.getUsername()) > 0;

            // create user account
            Account account = new Account(user.getUserId());

            // generate salt
            byte[] salt = generateSalt();
            // generate hash string
            String hashString = generateHashString(pwd, salt);
            // create user account
            newAccountCreated = jdbcTemplate.update(CREATE_NEW_ACCOUNT_SQL, account.getAccountId(), user.getUserId(), salt.toString(), hashString, account.getIsValid()) > 0;

            if (newUserCreated && newAccountCreated) {

                return account.getAccountId();

            } else {

                throw new AccountCreationException("Failed to generate user entity");

            }

        } else {

            throw new AccountCreationException("Invalid username");
        }

    }

    public Boolean verifyIfUsernameExist(String username) {

        // get all existing username
        List<String> usernames = jdbcTemplate.query(RETURN_ALL_USERNAME, BeanPropertyRowMapper.newInstance(String.class));

        return usernames.contains(username);
    }

    public byte[] generateSalt() {

        // generate salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    public String generateHashString(String pwd, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

        // perform password base encryption on salt with specific iteration
        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 10000, 128);

        // perform cryptographic operation
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return hash.toString();
    }
    
}
