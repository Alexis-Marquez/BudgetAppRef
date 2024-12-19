package budgetapprefactored.Accounts;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class AccountRequest {
    @NotNull
    private String type;

    @NotNull
    private String name;

    @Getter
    private BigDecimal balance;

    public AccountRequest() {
        type = "";
        name = "";
    }

    public @NotNull String getType() {
        return type;
    }

    public void setType(@NotNull String type) {
        this.type = type;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

}
