package iss.ibf.pfm_expenses_server.model;

public class GoogleAuthToken {

    private String access_token;
    private Integer expires_in;
    private String scope;
    private String token_type;

    public String getAccess_token() {
        return this.access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Integer getExpires_in() {
        return this.expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getToken_type() {
        return this.token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    @Override
    public String toString() {
        return "{" +
            " access_toke='" + getAccess_token() + "'" +
            ", expires_in='" + getExpires_in() + "'" +
            ", scope='" + getScope() + "'" +
            ", token_type='" + getToken_type() + "'" +
            "}";
    }
    
}
