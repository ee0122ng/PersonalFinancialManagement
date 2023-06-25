package iss.ibf.pfm_expenses_server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import iss.ibf.pfm_expenses_server.model.Account;

@Repository
public class AccountAuthenticationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String GET_USERID_SQL = "select user_id from users where username=?";
    private final String GET_ACCOUNT_STATUS_SQL = "select is_valid from accounts where user_id=?";
    private final String GET_ACCOUNT_SQL = "select * from accounts where user_id=?";


    public Boolean verifyIfAccountValid(String username) {

        String userId = this.getUserId(username);

        return jdbcTemplate.queryForList(GET_ACCOUNT_STATUS_SQL,Boolean.class, userId).get(0);
    }

    public Boolean verifyIfPasswordValid(String username, String pwd) {

        Account account = this.getAccount(this.getUserId(username));
        String hashedPwd = this.generateHashPwdByUsername(username, pwd);

        return account.getHashedString().equals(hashedPwd);

    }

    public String generateHashPwdByUsername(String username, String pwd) {

        Account account = this.getAccount(this.getUserId(username));
        // convert password to hash string
        String hashedPwd = BCrypt.hashpw(pwd, account.getSaltString());

        return hashedPwd;

    }

    public String getUserId(String username) {

        return jdbcTemplate.queryForList(GET_USERID_SQL, String.class, username).get(0);
    }

    public Account getAccount(String userId) {

        return jdbcTemplate.query(GET_ACCOUNT_SQL, BeanPropertyRowMapper.newInstance(Account.class), userId).get(0);
    }

}
