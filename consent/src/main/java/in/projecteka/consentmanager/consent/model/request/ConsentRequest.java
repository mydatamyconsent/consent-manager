package in.projecteka.consentmanager.consent.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@Builder
public class ConsentRequest {
    @Valid
    @NotNull(message = "Consent detail is not specified.")
    private RequestedDetail consent;
    private UUID requestId;
    private LocalDateTime timestamp;
}
