import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class Webshop {

    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("src/db.properties")) {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            // 1. Vilka kunder har köpt svarta byxor i storlek 38 av märket TigerOfTheJungle?
            String query1 = "SELECT Customers.FirstName, Customers.LastName " +
                    "FROM Customers " +
                    "INNER JOIN Orders ON Customers.CustomerID = Orders.CustomerID " +
                    "INNER JOIN OrderDetails ON Orders.OrderID = OrderDetails.OrderID " +
                    "INNER JOIN Products ON OrderDetails.ProductID = Products.ProductID " +
                    "WHERE Products.Name = 'Jeans' " +
                    "AND Products.Color = 'Black' " +
                    "AND Products.Size = '38' " +
                    "AND Products.Brand = 'TigerOfTheJungle';";
            try (PreparedStatement pstmt = conn.prepareStatement(query1);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Customers who bought black jeans size 38 from 'TigerOfTheJungle':");
                while (rs.next()) {
                    System.out.println("Customer: " + rs.getString("FirstName") + " " + rs.getString("LastName"));
                }
            }

            // 2. Hur många produkter finns i kategorin...
            String query2 = "SELECT Category.Namn, count(ProductCategory.ProductID) AS Quantity " +
                    "FROM Category " +
                    "LEFT JOIN productcategory ON category.CategoryID = productcategory.CategoryID " +
                    "GROUP BY Category.Namn;";
            try (PreparedStatement pstmt = conn.prepareStatement(query2);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nNumber of products in each category:");
                while (rs.next()) {
                    System.out.println("Category: " + rs.getString("Namn") + " - Quantity: " + rs.getInt("Quantity"));
                }
            }

            // 3. Kundlista med den totala summan pengar som varje kund har handlat för.
            String query3 = "SELECT Customers.FirstName, Customers.LastName, " +
                    "SUM(OrderDetails.PurchasePrice * OrderDetails.Quantity) AS TotalSpent " +
                    "FROM Customers " +
                    "JOIN Orders ON Customers.CustomerID = orders.CustomerID " +
                    "JOIN OrderDetails ON Orders.OrderID = orderdetails.OrderID " +
                    "GROUP BY customers.FirstName, Customers.LastName;";
            try (PreparedStatement pstmt = conn.prepareStatement(query3);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nTotal money spent by each customer:");
                while (rs.next()) {
                    System.out.println("Customer: " + rs.getString("FirstName") + " " + rs.getString("LastName") +
                            " - Total Spent: " + rs.getDouble("TotalSpent"));
                }
            }

            // 4. Det totala beställningsvärdet per stad där beställningsvärdet är större än 1000 kr
            String query4 = "SELECT Customers.city, SUM(orderdetails.PurchasePrice * orderdetails.Quantity) AS TotalValue " +
                    "FROM Customers " +
                    "JOIN orders ON Customers.CustomerID = orders.CustomerID " +
                    "JOIN orderdetails ON orders.OrderID = orderdetails.orderID " +
                    "GROUP BY Customers.City " +
                    "HAVING TotalValue > 1000;";
            try (PreparedStatement pstmt = conn.prepareStatement(query4);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nTotal order value per city (over 1000):");
                while (rs.next()) {
                    System.out.println("City: " + rs.getString("city") + " - Total Value: " + rs.getDouble("TotalValue"));
                }
            }

            // 5. Topp-5 lista av de mest sålda produkterna
            String query5 = "SELECT products.name, SUM(orderDetails.Quantity) AS SoldAmount " +
                    "FROM Products " +
                    "JOIN orderdetails ON products.ProductID = orderdetails.ProductID " +
                    "GROUP BY Products.name " +
                    "ORDER BY SoldAmount DESC " +
                    "LIMIT 5;";
            try (PreparedStatement pstmt = conn.prepareStatement(query5);
                 ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\nTop 5 most sold products:");
                while (rs.next()) {
                    System.out.println("Product: " + rs.getString("name") + " - Sold Amount: " + rs.getInt("SoldAmount"));
                }
            }

            // 6. Vilken månad hade störst försälning?
            String query6 = "SELECT Date_Format(Orders.Date, '%Y, %M') AS Month, " +
                    "SUM(orderdetails.PurchasePrice * orderdetails.Quantity) AS TotalSales " +
                    "FROM orders " +
                    "JOIN orderdetails ON orders.OrderID = orderdetails.OrderID " +
                    "GROUP BY Month " +
                    "ORDER BY TotalSales DESC " +
                    "LIMIT 1;";
            try (PreparedStatement pstmt = conn.prepareStatement(query6);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nMonth with the highest sales:");
                    System.out.println("Month: " + rs.getString("Month") + " - Total Sales: " + rs.getDouble("TotalSales"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
