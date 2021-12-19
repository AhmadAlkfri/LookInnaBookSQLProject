import java.sql.*;
import java.util.*;

public class LookInnaBook {

    private static void printBookSet(ResultSet rs, Connection connection){
        System.out.println("RESULTS:");
        try {
            while (rs.next()) {
                String title = "", data = "", author = "";
                System.out.println("----------");
                title = (rs.getString("title") + "\n");
                data = ("Price: $" + rs.getString("price") + "\nGenre: " + rs.getString("genre") + "\nIn Stock: " + rs.getString("in_stock") + "\n");
                String query = "select name from wrote inner join author on wrote.author_id=author.id where isbn = '" + rs.getString("isbn") + "';";
                try {
                    Statement stmt = connection.createStatement();
                    ResultSet rs2 = stmt.executeQuery(query);
                    author = ("Author(s):\n");
                    while (rs2.next()) {
                        author = (author + "    " + rs2.getString("name") + "\n");
                    }
                } catch (SQLException e) {
                    System.out.println(e);
                }
                System.out.println(title + author + data);
            }
        }catch(SQLException e) {
            System.out.println(e);
        }
    }

    private static boolean isbnValidate(String isbn){
        if(isbn.length() != 13){return false;}
        try{
            Long.parseLong(isbn);
        }catch(Exception e){
            return false;
        }
        int sum = 0;
        for(int i = 0; i<13; i++){
            if(i%2 == 0) {
                sum = sum + Integer.parseInt(String.valueOf(isbn.charAt(i)));
            }else{
                sum = sum + 3 * Integer.parseInt(String.valueOf(isbn.charAt(i)));
            }
        }
        if(sum % 10 == 0){
            return true;
        }else{
            return false;
        }
    }

    private static void search(Scanner sc, Connection connection){
        boolean exit = false;
        while(!exit) {
            System.out.println("1. Search by ISBN");
            System.out.println("2. Search by Title");
            System.out.println("3. Search by Author");
            System.out.println("4. Search by Genre");
            System.out.println("-1. Exit");
            String choice = sc.nextLine();
            String query = "";
            switch (choice) {
                case "1":
                    System.out.print("Please enter the 13 digit ISBN (no dashes or spaces): ");
                    String isbn = sc.nextLine();
                    if(isbnValidate(isbn)) {
                        query = "select * from book where isbn = '" + isbn + "';";
                        try {
                            Statement stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery(query);
                            printBookSet(rs, connection);
                        }catch(SQLException e) {
                            System.out.println(e);
                        }
                    }else {
                        System.out.println("Invalid ISBN!");
                    }
                    break;
                case "2":
                    boolean valid = false;
                    while(!valid) {
                        System.out.println("Please select: Broad Search (1) or Narrow Search (2): ");
                        choice = sc.nextLine();
                        String title;
                        switch (choice) {
                            case "1":
                                System.out.print("Please enter the title of the book: ");
                                title = sc.nextLine();
                                String keywords[] = title.split(" ");
                                query = "select * from book where title ";
                                for (int i = 0; i < keywords.length; i++) {
                                    if (i == 0) {
                                        query += "LIKE '%" + keywords[i] + "%' ";
                                    } else {
                                        query += "OR title LIKE '%" + keywords[i] + "%' ";
                                    }
                                }
                                query += ";";
                                valid = true;
                                break;
                            case "2":
                                System.out.print("Please enter the title of the book: ");
                                title = sc.nextLine();
                                query = "select * from book where title LIKE '%" + title + "%';";
                                valid = true;
                                break;
                        }
                    }
                    try {
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        printBookSet(rs, connection);
                    }catch(SQLException e) {
                        System.out.println(e);
                    }
                    break;
                case "3":
                    System.out.print("Please enter the author: ");
                    String author = sc.nextLine();
                    query = "select * from book natural join (wrote inner join author on wrote.author_id=author.id) where name LIKE '%" + author + "%';";
                    try {
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        printBookSet(rs, connection);
                    }catch(SQLException e) {
                        System.out.println(e);
                    }

                    break;
                case "4":
                    System.out.print("Please enter the genre: ");
                    String genre = sc.nextLine();
                    query = "select * from book where genre LIKE '%" + genre + "%';";
                    try {
                        Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        printBookSet(rs, connection);
                    }catch(SQLException e) {
                        System.out.println(e);
                    }
                    break;
                case "-1":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid Entry!");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        boolean loggedin = false;
        boolean isadmin = false;
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "apple");
        }catch (Exception e){
            System.out.println(e);
        }
        Scanner sc= new Scanner(System.in);
        while(true){
            System.out.println("Welcome to LookInnaBook! Please select an option:");
            System.out.println("1. Search LookInnaBook");
            if(!loggedin) {
                System.out.println("2. Log In");
                System.out.println("3. Register");
            }else{
                System.out.println("2. Log Out");
            }
            String choice = sc.nextLine();

            switch(choice){
                case "1":
                    search(sc, connection);
                    break;
                case "2":
                    break;
                default:
                    System.out.println("Invalid Entry!");
                    break;
            }
        }

        /*for(int i = 0; i<list.size(); i++){
            ID = list.get(i);
            query = "SELECT prereq_id FROM prereq WHERE course_id ='" + ID + "';";
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()) {
                    pre = rs.getString("prereq_id");
                    System.out.println(ID + " Requires -> " + pre);
                    if(alreadyListed.add(pre)){
                        list.add(pre);
                    }
                }
            }catch(SQLException e) {
                System.out.println(e);
            }
        }*/
        //System.out.println("------");
    }
}
