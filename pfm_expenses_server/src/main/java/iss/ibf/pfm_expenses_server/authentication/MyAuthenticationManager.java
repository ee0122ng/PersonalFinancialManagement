package iss.ibf.pfm_expenses_server.authentication;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import iss.ibf.pfm_expenses_server.repository.AccountAuthenticationRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class MyAuthenticationManager implements AuthenticationManager {

    @Autowired
    private AccountAuthenticationRepository accAuthRepo;

    
    public Authentication generateAuthEntity(String username, String pwd) {
        
        // convert pwd to hashpassword then generate a auth entity
        String hashedPwd = this.accAuthRepo.generateHashPwdByUsername(username, pwd);

        List<SimpleGrantedAuthority> roles = List.of("USER").stream().map(r -> new SimpleGrantedAuthority(r)).toList();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, hashedPwd, roles);

        return this.authenticate(authToken);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        String username = authentication.getName();
        String hashpwd = String.valueOf(authentication.getCredentials());

        if (this.accAuthRepo.verifyIfAccountValid(username) && this.accAuthRepo.getAccount(this.accAuthRepo.getUserId(username)).getHashedString().equals(hashpwd)) {

            // set th authentication to current framework
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(authentication);
            SecurityContextHolder.setContext(sc);

            return authentication;

        } else {
            return null;
        }
        
    }
}
