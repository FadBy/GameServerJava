package marat.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClientResponse {
    private final int CodeStatus;
    private final ClientRequest Request;
    private final String Body;
}
