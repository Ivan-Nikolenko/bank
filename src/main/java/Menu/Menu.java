package Menu;

import entity.Account;
import entity.Card;
import entity.Client;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class Menu {

    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "qwerty";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/bank?serverTimezone=UTC";

    Scanner scanner = new Scanner(System.in);

    public void mainMenu(Client client) {

        System.out.println("\nВас приветствует ВашБанк!");

        while (true) {
            System.out.println("\nВыберите одно из предложенных действий:");
            System.out.println("1. Пополнить счёт");
            System.out.println("2. Снять наличные");
            System.out.println("3. Просмотр имеющихся карт");
            System.out.println("4. Перевод денег");
            System.out.println("5. Выход");

            int choose = scanner.nextInt();

            switch (choose) {
                case 1:
                    addToBalance(client);
                    break;

                case 2:
                    writeOffCash(client);
                    break;

                case 3:
                    System.out.println("Ваши имеющиеся карты:");
                    getAllCards(client);
                    break;

                case 4:
                    cashTransfer(client);
                    break;

                case 5:
                    System.out.println("Завершение работы приложения");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Не вверно введено число, попробуйте ещё раз");
            }
        }
    }

    public void addToBalance(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Ваши карты:");
            getAllCards(client);

            System.out.println("\nВведите номер карты для пополнения:");
            scanner.nextLine();
            String card = scanner.nextLine();

            System.out.println("\nВведите сумму для пополнения:");
            String cash = scanner.nextLine();
            BigDecimal cashToAdd = BigDecimal.valueOf(Long.parseLong(cash));

            client.getAccountList()
                    .stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(card))
                    .forEach(o -> o.setBalance(o.getBalance().add(cashToAdd)));

            BigDecimal cashToAddMethod = client.getAccountList().stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(card))
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String SQL_ADD_ACCOUNT_BALANCE_BY_CARD_NUMBER = String.format("update account a join cards c on a.account_id = c.account_id set balance = %s\n" +
                    "where c.number = '%s'\n", cashToAddMethod, card);

            Statement statement = connection.createStatement();

            statement.executeUpdate(SQL_ADD_ACCOUNT_BALANCE_BY_CARD_NUMBER);

            String ADD_TRANSACTION = String.format("insert into transactions (amount, type, card_id)\n" +
                    "values ((%s), ('accural'), (\n" +
                    "    select card_id\n" +
                    "    from cards\n" +
                    "    where cards.number = '%s'))\n", cashToAdd, card);

            statement.executeUpdate(ADD_TRANSACTION);

            System.out.println("Счёт успешно пополнен!\n");
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

    public void writeOffCash(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Ваши карты:");
            getAllCards(client);

            System.out.println("\nВведите номер карты для снятия:");
            String temp = scanner.nextLine();
            String card = scanner.nextLine();

            System.out.println("\nВведите сумму для снятия:");
            String cash = scanner.nextLine();
            BigDecimal cashToAdd = BigDecimal.valueOf(Long.parseLong(cash));

            client.getAccountList()
                    .stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(card))
                    .forEach(o -> o.setBalance(o.getBalance().subtract(cashToAdd)));

            BigDecimal cashToAddMethod = client.getAccountList().stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(card))
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String SQL_ADD_ACCOUNT_BALANCE_BY_CARD_NUMBER = String.format("update account a join cards c on a.account_id = c.account_id set balance = %s\n" +
                    "where c.number = '%s'\n", cashToAddMethod, card);


            Statement statement = connection.createStatement();

            statement.executeUpdate(SQL_ADD_ACCOUNT_BALANCE_BY_CARD_NUMBER);

            String ADD_TRANSACTION = String.format("insert into transactions (amount, type, card_id)\n" +
                    "values ((%s), ('write-off'), (\n" +
                    "    select card_id\n" +
                    "    from cards\n" +
                    "    where cards.number = '%s'))\n", cashToAdd, card);

            statement.executeUpdate(ADD_TRANSACTION);

            System.out.println("Списание успешно произведено!\n");
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

    public void getAllCards(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String SQL_ADD_ALL_CARTS = String.format("select c2.number, balance\n" +
                    "from clients c\n" +
                    "         join account a on c.client_id = a.client_id\n" +
                    "         join cards c2 on a.account_id = c2.account_id\n" +
                    "where a.client_id = '%d'", client.getId());

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(SQL_ADD_ALL_CARTS);
            int i = 1;
            while (resultSet.next()) {
                System.out.println("\nКарта №" + i + ".\n" +
                        "Номер карты: " + resultSet.getString("number") +
                        "\nБаланс карты: " + resultSet.getBigDecimal("balance")
                        + " рублей");
                i++;
            }
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

    public void cashTransfer(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Ваши карты:");
            getAllCards(client);

            String cardFrom = "";
            String cardTo = "";


            BigDecimal cashToAdd = null;

            boolean balanceIsValid = true;

            while (balanceIsValid) {
                scanner.nextLine();
                System.out.println("Введите номер карты с которой хотите осуществить перевод:");
                cardFrom = scanner.nextLine();

                String cardFromTemp = cardFrom;

                System.out.println("Введите номер карты на которую хотите осуществить перевод:");
                cardTo = scanner.nextLine();

                System.out.println("Введите сумму, которую хотите перевести:");
                cashToAdd = scanner.nextBigDecimal();


                if (client.getAccountList().stream()
                        .filter(o -> o.getClient().getId() == client.getId())
                        .filter(o -> o.getCard().getNumber().equals(cardFromTemp))
                        .map(Account::getBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(cashToAdd) >= 0) {
                    balanceIsValid = false;
                } else {
                    System.out.println("На вашем счету недостаточно средств для перевода, выберите другую карту!");
                }
            }

            String cardFromTemp = cardFrom;
            String cardToTemp = cardTo;

            String SQL_WRITE_OFF = String.format("update account a join cards c on a.account_id = c.account_id set balance = %s\n" +
                    "where c.number = '%s'\n", (client.getAccountList().stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(cardFromTemp))
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).subtract(cashToAdd)), cardFrom);

            String SQL_ADD_TO_BALANCE = String.format("update account a join cards c on a.account_id = c.account_id set balance = %s\n" +
                    "where c.number = '%s'\n", cashToAdd.add(client.getAccountList().stream()
                    .filter(o -> o.getClient().getId() == client.getId())
                    .filter(o -> o.getCard().getNumber().equals(cardToTemp))
                    .map(Account::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)), cardTo);



            String SQL_TRANSACTION_TO_ADD = String.format("insert into transactions (amount, type, card_id)\n" +
                    "values ((%s), ('accural'), (\n" +
                    "    select card_id\n" +
                    "    from cards\n" +
                    "    where cards.number = '%s'))\n", cashToAdd, cardTo);

            String SQL_TRANSACTION_TO_WRITE_OFF = String.format("insert into transactions (amount, type, card_id)\n" +
                    "values ((%s), ('write-off'), (\n" +
                    "    select card_id\n" +
                    "    from cards\n" +
                    "    where cards.number = '%s'))\n", cashToAdd, cardFrom);

            Statement statement = connection.createStatement();

            statement.executeUpdate(SQL_WRITE_OFF);
            statement.executeUpdate(SQL_ADD_TO_BALANCE);
            statement.executeUpdate(SQL_TRANSACTION_TO_ADD);
            statement.executeUpdate(SQL_TRANSACTION_TO_WRITE_OFF);

            System.out.println("Перевод успешно выполнен!");

        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

}
