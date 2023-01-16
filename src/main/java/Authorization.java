import Menu.Menu;
import entity.Account;
import entity.Card;
import entity.Client;
import entity.Document;

import java.sql.*;
import java.util.Scanner;

public class Authorization {

    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "qwerty";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/bank?serverTimezone=UTC";
    Scanner scanner = new Scanner(System.in);

    public void auth() throws ClassNotFoundException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            Client client = new Client();

            while (true) {
                System.out.println("Введите логин, для входа в приложение:");
                String login = scanner.nextLine();
                System.out.println("Введите пароль, для входа в приложение");
                String password = scanner.nextLine();

                Statement statement = connection.createStatement();

                String SQL_AUTHORIZATION = String.format("select client_id, surname, name, patronymic, birth_date, phone_number, address\n" +
                        "from clients\n" +
                        "         join authorization a on a.authorization_id = clients.authorization_id\n" +
                        "where login = '%s'\n" +
                        "  and password = '%s';", login, password);

                ResultSet resultSet = statement.executeQuery(SQL_AUTHORIZATION);
                if (resultSet.next()) {
                    client.setId(resultSet.getInt("client_id"));
                    client.setName(resultSet.getString("name"));
                    client.setSurname(resultSet.getString("surname"));
                    client.setPatronymic(resultSet.getString("patronymic"));
                    client.setAddress(resultSet.getString("address"));
                    client.setPhoneNumber(resultSet.getString("phone_number"));

                    connection.close();
                    addAccounts(client);
                    addDocument(client);

                    Menu menu = new Menu();
                    menu.mainMenu(client);
                } else {
                    System.out.println("\nНеверные данные для входа, введите ещё раз\n");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addAccounts(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String SQL_ADD_ACCOUNTS = String.format("select account_id, number, balance\n" +
                    "from account\n" +
                    "where client_id = '%d'", client.getId());

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL_ADD_ACCOUNTS);

            while (resultSet.next()) {
                Account account = new Account();
                account.setId(resultSet.getInt("account_id"));
                account.setBalance(resultSet.getBigDecimal("balance"));
                account.setNumber(resultSet.getString("number"));
                account.setClient(client);

                addCards(account);

                client.setToAccountList(account);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addCards(Account account) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String SQL_ADD_CARDS = String.format("select card_id, number\n" +
                    "from cards\n" +
                    "where account_id = '%d'", account.getId());

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(SQL_ADD_CARDS);

            while (resultSet.next()) {
                Card card = new Card();
                card.setId(resultSet.getInt("card_id"));
                card.setNumber(resultSet.getString("number"));

                account.setCard(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDocument(Client client) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String SQL_ADD_DOCUMENT = String.format("select c.document_id, type, number\n" +
                    "from clients c join documents d on d.document_id = c.document_id\n" +
                    "where client_id = '%d';\n", client.getId());

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(SQL_ADD_DOCUMENT);

            while (resultSet.next()) {
                Document document = new Document();
                document.setId(resultSet.getInt("document_id"));
                document.setType(resultSet.getString("type"));
                document.setNumber(resultSet.getString("number"));
                client.setDocument(document);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

