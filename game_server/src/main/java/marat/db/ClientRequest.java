package marat.db;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ClientRequest implements Serializable {
    // private static final long serialVersionUID = 1L;

    private final String RequestType;

    private final String Body;

    
}
