package iss.ibf.pfm_expenses_server.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import iss.ibf.pfm_expenses_server.model.Account;
import iss.ibf.pfm_expenses_server.model.UserDetails;

@Repository
public class ProfileRetrievalRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String GET_USER_PROFILE_SQL = "select * from user_details where user_id=?";
    private final String GET_USER_ID_SQL = "select * from accounts where account_id=?";

    public UserDetails retrieveUserProfile(String accId) {

        UserDetails userDetails = this.jdbcTemplate.queryForObject(GET_USER_PROFILE_SQL, BeanPropertyRowMapper.newInstance(UserDetails.class), this.getUserIdByAccId(accId));

        return userDetails;
    }

    public String getUserIdByAccId(String accId) {

        Account acc = this.jdbcTemplate.queryForObject(GET_USER_ID_SQL, BeanPropertyRowMapper.newInstance(Account.class), accId);

        return acc.getUserId();
    }

}
