package donatehub.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import donatehub.domain.enums.UserRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRes {
    private UserRole role;
    private String accessToken;
    private String refreshToken;
}