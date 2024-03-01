package marat.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class PlayerProfileTable {
    private final int Id;
    private final String Nickname;
    private final LocalDateTime RegisterDate;
    private final LocalDateTime LastLoginDate;
    private final boolean BanStatus;
    private final float MoneyAmount;
}
