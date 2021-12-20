import java.sql.*;
import java.util.*;

public class LookInnaBook {

    static HashSet<String> validProvinces = new HashSet<>();

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

    private static ArrayList orderSearchResults(ResultSet rs, Connection connection){
        ArrayList<String> results = new ArrayList();
        int count = 0;
        System.out.println("RESULTS:");
        try{
            while (rs.next()) {
                System.out.println(count + ". " + rs.getString("title"));
                results.add(rs.getString("isbn"));
                results.add(rs.getString("in_stock"));
                count++;
            }
        }catch(SQLException e) {
            System.out.println(e);
        }
        return results;
    }

    private static void printCart(Hashtable cart, Connection connection){
        System.out.println("Your Cart: ");
        Object isbns[] = cart.keySet().toArray();
        for(int i = 0; i< cart.size(); i++) {
            String query = "select * from book where isbn = '" + isbns[i] + "';";
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()){
                    System.out.println(cart.get(isbns[i]) + "x - " + rs.getString("title"));
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        System.out.println("-----");
    }

    private static int generateOrderNum(Connection connection){
        int id = 0;
        boolean exit = false;
        while(!exit) {
            Random rnd = new Random();
            id = 10000000 + rnd.nextInt(90000000);
            exit = true;
            String query = "select * from orders where id='" + id + "';";
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    if(rs.getInt("id") == id){
                        exit = false;
                    }
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        return id;
    }

    private static double getOrderTotal(Hashtable cart, Connection connection){
        double total = 0;
        Object[] isbns = cart.keySet().toArray();
        for(int i = 0; i< cart.size(); i++) {
            String query = "select * from book where isbn = '" + isbns[i] + "';";
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                while(rs.next()){
                    total += rs.getDouble("price");
                }
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        return total;
    }

    private static void checkout(Scanner sc, Connection connection, String username, Hashtable cart){
        String choice, prov = "", city = "", address = "", postal = "";
        while(true) {
            System.out.println("Use user data to checkout (Y/N)?");
            choice = sc.nextLine().toUpperCase();
            if(choice.equals("Y") || choice.equals("N")){
                break;
            }else{
                System.out.println("Invalid Option.");
            }
        }
        switch(choice){
            case "Y":
                String query = "select * from users where username='" +username+"';";
                try {
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        prov = rs.getString("province");
                        city = rs.getString("city");
                        address = rs.getString("address");
                        postal = rs.getString("postal_code");
                    }
                } catch (SQLException e) {
                    System.out.println(e);
                }
                break;
            case "N":
                while(true) {
                    System.out.println("Please enter your Province (2 Char format): ");
                    prov = sc.nextLine().toUpperCase();
                    if (!(validProvinces.contains(prov))) {
                        System.out.println("Invalid Province! (Valid Provinces: NL, PE, NS, NB, QC, ON, MB, SK, AB, BC, YT, NT, NU)");
                    }else{
                        break;
                    }
                }
                System.out.println("Please enter your City: ");
                city = sc.nextLine();
                System.out.println("Please enter your address: ");
                address = sc.nextLine();
                while(true) {
                    System.out.println("Please enter your postal code (no dashes or spaces): ");
                    postal = sc.nextLine();
                    if (postal.length() != 6) {
                        System.out.println("Invalid Postal Code!");
                    }else{
                        break;
                    }
                }
                break;
        }
        printCart(cart, connection);
        double orderTotal = getOrderTotal(cart, connection);
        System.out.println("Total Cost $" + orderTotal);
        System.out.println("Confirm this order? (Y)");
        choice = sc.nextLine().toUpperCase();
        if(choice.equals("Y")){
            int orderId = generateOrderNum(connection);
            int month = Calendar.getInstance().get(Calendar.MONTH)+1;
            int year = Calendar.getInstance().get(Calendar.YEAR);
            System.out.println("Order ID: " + orderId);
            String query = "insert into orders values('"+orderId+"', '"+username+"', '"+orderId+"', '"+prov+"', '"+city+"', '"+address+"', '"+postal+"', " + orderTotal + ", " + month + ", " + year + ");";
            try {
                Statement stmt = connection.createStatement();
                stmt.execute(query);
            } catch (SQLException e) {
                System.out.println(e);
            }
            Object[] isbns = cart.keySet().toArray();
            for(int i = 0; i< cart.size(); i++) {
                query = "insert into cart values('"+isbns[i]+"', '"+orderId+"', '"+cart.get(isbns[i])+"');";
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(query);
                } catch (SQLException e) {
                    System.out.println(e);
                }
                query = "update book set num_sold = num_sold +" + cart.get(isbns[i]) + "where isbn='"+ isbns[i]+"'";
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(query);
                } catch (SQLException e) {
                    System.out.println(e);
                }
                query = "update book set in_stock = in_stock - " + cart.get(isbns[i]) + "where isbn='"+ isbns[i]+"'";
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(query);
                } catch (SQLException e) {
                    System.out.println(e);
                }
                query = "update author set num_sold = num_sold +" + cart.get(isbns[i]) + "where id = (select author_id from wrote where isbn = '"+ isbns[i]+ "');";
                try {
                    Statement stmt = connection.createStatement();
                    stmt.execute(query);
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        }else{
            System.out.println("Order Canceled");
            return;
        }
    }

    private static void order(Scanner sc, String username, Connection connection){
        boolean exit = false;
        Hashtable<String, Integer> cart = new Hashtable<>();
        while(!exit) {
            ArrayList<String> results = new ArrayList<>();
            System.out.println("1. Search by ISBN");
            System.out.println("2. Search by Title");
            System.out.println("3. Search by Author");
            System.out.println("4. Search by Genre");
            System.out.println("-1. Exit");
            System.out.println("-2. Checkout");
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
                            results = orderSearchResults(rs, connection);
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
                        results = orderSearchResults(rs, connection);
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
                        results = orderSearchResults(rs, connection);
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
                        results = orderSearchResults(rs, connection);
                    }catch(SQLException e) {
                        System.out.println(e);
                    }
                    break;
                case "-1":
                    System.out.println("Order Canceled!");
                    exit = true;
                    break;
                case "-2":
                    checkout(sc, connection, username, cart);
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid Entry!");
                    break;
            }
            if(!exit && results.size() > 0) {
                System.out.println("Enter item # to add to cart (enter -1 to add nothing): ");
                choice = sc.nextLine();
                try {
                    int item = Integer.parseInt(choice);
                    item = 2*item;
                    if (item >= 0 && item < results.size()) {
                        int instock;
                        if(cart.containsKey(results.get(item))) {
                             instock = (Integer.parseInt(results.get(item + 1)) - cart.get(results.get(item)));
                        }else{
                            instock = (Integer.parseInt(results.get(item + 1)));
                        }
                        System.out.println("Quantity to add to cart (" +  instock + " in stock): ");
                        choice = sc.nextLine();
                        try {
                            int quantity = Integer.parseInt(choice);
                            if (quantity > instock) {
                                System.out.println("That is too many! Nothing added to cart");
                            } else {
                                if (!cart.containsKey(results.get(item))) {
                                    cart.put(results.get(item), quantity);
                                } else {
                                    cart.replace(results.get(item), quantity + cart.get(results.get(item)));
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid choice, nothing added");
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println("Invalid choice, nothing added");
                }
                printCart(cart, connection);
            }
        }
    }

    private static void printDetailedBookSet(ResultSet rs, Connection connection){
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
            System.out.println("-------");
        }catch(SQLException e) {
            System.out.println(e);
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
                            printDetailedBookSet(rs, connection);
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
                        printDetailedBookSet(rs, connection);
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
                        printDetailedBookSet(rs, connection);
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
                        printDetailedBookSet(rs, connection);
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

    private static String login(Scanner sc, Connection connection){
        String username, password;
        System.out.println("Please enter your Username: ");
        username = sc.nextLine();
        System.out.println("Please enter your Password: ");
        password = sc.nextLine();
        String query = "select * from users where username='" +username+"' AND password='"+password+"';";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        System.out.println("Invalid Username/Password");
        return null;
    }

    private static boolean checkAdmin(String username, Connection connection){
        String query = "select is_admin from users where username='" + username + "';";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                return rs.getBoolean("is_admin");
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    private static String addUser(Scanner sc, Connection connection){
        String username, password, prov, city, address, postal;
        System.out.println("Please enter your Username (15 Char MAX): ");
        username = sc.nextLine();
        if(username.length() > 15){
            System.out.println("Invalid Username!");
            return null;
        }
        System.out.println("Please enter your Password (15 Char MAX): ");
        password = sc.nextLine();
        if(password.length() > 15){
            System.out.println("Invalid Password!");
            return null;
        }
        System.out.println("Please enter your Province (2 Char format): ");
        prov = sc.nextLine().toUpperCase();
        if(!(validProvinces.contains(prov))){
            System.out.println("Invalid Province! (Valid Provinces: NL, PE, NS, NB, QC, ON, MB, SK, AB, BC, YT, NT, NU)");
            return null;
        }
        System.out.println("Please enter your City: ");
        city = sc.nextLine();
        System.out.println("Please enter your address: ");
        address = sc.nextLine();
        System.out.println("Please enter your postal code (no dashes or spaces): ");
        postal = sc.nextLine();
        if(postal.length() != 6){
            System.out.println("Invalid Postal Code!");
            return null;
        }
        String query = "insert into users values('"+username+"', '"+password+"', '"+prov+"', '"+city+"', '"+address+"', '"+postal+"', "+false+");";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            System.out.println("Success.");
            return username;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    private static void addBook(Scanner sc, Connection connection){
        String isbn, title, genre;
        double price;
        int num_pages, pub_id, percent_sales, author_id, in_stock, copies;
        System.out.println("Enter Book's ISBN: ");
        isbn = sc.nextLine();
        if(!isbnValidate(isbn)){
            System.out.println("Invalid ISBN!");
            return;
        }
        System.out.println("Enter the title");
        title = sc.nextLine();
        System.out.println("Enter Book price: $");
        try{
            price = sc.nextDouble();
        }catch (Exception e){
            System.out.println("Invalid Price");
            return;
        }
        sc.nextLine();
        System.out.println("Enter the genre: ");
        genre = sc.nextLine();
        System.out.println("Enter Book page count: ");
        try{
            num_pages = sc.nextInt();
        }catch (Exception e){
            System.out.println("Invalid Page Count!");
            return;
        }
        System.out.println("Enter author ID: ");
        try{
            author_id = sc.nextInt();
        }catch (Exception e){
            System.out.println("Invalid ID!");
            return;
        }
        System.out.println("Enter publisher ID: ");
        try{
            pub_id = sc.nextInt();
        }catch (Exception e){
            System.out.println("Invalid ID!");
            return;
        }
        System.out.println("Enter % sales the publisher takes: ");
        try{
            percent_sales = sc.nextInt();
            if(percent_sales > 100 || percent_sales < 0){
                System.out.println("Invalid percentage!");
                return;
            }
        }catch (Exception e){
            System.out.println("Invalid percentage!");
            return;
        }
        System.out.println("How many copies in stock: ");
        try{
            copies = sc.nextInt();
            if(copies < 0){
                System.out.println("Invalid!");
                return;
            }
        }catch (Exception e){
            System.out.println("Invalid!");
            return;
        }
        String query = "insert into book values("+isbn+", '"+title+"', "+price+", "+num_pages+", '"+genre+"', "+copies+", 0,"+pub_id+","+percent_sales+");";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(query);
            query = "insert into wrote values(" +isbn+", " +author_id+");";
            try {
                stmt = connection.createStatement();
                stmt.execute(query);
                System.out.println("Success.");
            } catch (SQLException e) {
                System.out.println(e);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }

    }

    public static void main(String[] args) {
        Collections.addAll(validProvinces, "NL", "PE", "NS", "NB", "QC", "ON", "MB", "SK", "AB", "BC", "YT", "NT", "NU");
        boolean loggedIn = false;
        String curUser = null;
        boolean isAdmin = false;
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
            if(!loggedIn) {
                System.out.println("2. Log In");
                System.out.println("3. Register");
            }else{
                System.out.println("2. Log Out");
                //System.out.println("3. View Profile");
                System.out.println("4. Start an order");
                if(isAdmin){
                    System.out.println("5. Add a book");
                }
            }
            String choice = sc.nextLine();

            switch(choice){
                case "1":
                    search(sc, connection);
                    break;
                case "2":
                    if(!loggedIn) {
                        curUser = login(sc, connection);
                        if(curUser != null){
                            loggedIn = true;
                            isAdmin = checkAdmin(curUser, connection);
                            if(isAdmin){
                                System.out.println("Admin Privileges Enabled!");
                            }
                        }
                    }else{
                        loggedIn = false;
                        curUser = null;
                        isAdmin = false;
                    }
                    break;
                case "3":
                    if(!loggedIn){
                        curUser = addUser(sc, connection);
                        if(curUser != null){
                            loggedIn = true;
                            isAdmin = checkAdmin(curUser, connection);
                            if(isAdmin){
                                System.out.println("Admin Privileges Enabled!");
                            }
                        }
                    }else{
                        //viewProfile(curUser, connection);
                    }
                    break;
                case "4":
                    if(loggedIn){
                        order(sc, curUser, connection);
                    }else{
                        System.out.println("Invalid Entry!");
                    }
                    break;
                case "5":
                    if(isAdmin){
                        addBook(sc, connection);
                    }else{
                        System.out.println("Invalid Entry!");
                    }
                    break;
                default:
                    System.out.println("Invalid Entry!");
                    break;
            }
        }

    }
}
