package marat.db;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class PlayerIdentity implements Serializable {
    @Getter
    private final String Nickname;
}
