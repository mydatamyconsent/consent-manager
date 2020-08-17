package in.projecteka.library.clients.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class KeyCloakUserPasswordChangeRequest {
    private final String value;
}
