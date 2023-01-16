package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Client {
    private int id;

    private String surname;

    private String name;

    private String patronymic;

    private String phoneNumber;

    private String address;

    Document document;

    List<Account> accountList;

    public void setToAccountList(Account account) {
        if (accountList == null) {
            accountList = new ArrayList<>();
        }
        accountList.add(account);
    }

}
