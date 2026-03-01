package es.wrapitup.wrapitup_planner.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import lombok.Data;

@Data
public class UserModelDTO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    private String image;
    private List<String> roles;
    private UserStatus status;
}
