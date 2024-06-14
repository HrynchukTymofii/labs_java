package org.example;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private Connection con;

    public void initialization(String name){
        try{
            Class.forName("org.sqlite.JDBC");
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
            con = DriverManager.getConnection("jdbc:sqlite:" + name);

            PreparedStatement st1 = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS 'product_group' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'title' TEXT);"
            );
            st1.executeUpdate();

            PreparedStatement st2 = con.prepareStatement("CREATE TABLE IF NOT EXISTS 'product' (" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'title' TEXT, " +
                    "'amount' INTEGER, " +
                    "'price' REAL, " +
                    "'group_id' INTEGER, " +
                    "FOREIGN KEY(group_id) REFERENCES product_group(id));");
            st2.executeUpdate();
        }catch(ClassNotFoundException e){
            System.out.println("Не знайшли драйвер JDBC");
            e.printStackTrace();
            System.exit(0);
        }catch (SQLException e){
            System.out.println("Не вірний SQL запит");
            e.printStackTrace();
        }
    }

    public void createProduct(String title, int amount, double price, int groupId) {
        try {
            PreparedStatement st = con.prepareStatement("INSERT INTO product(title, amount, price, group_id) VALUES(?, ?, ?, ?)");
            st.setString(1, title);
            st.setInt(2, amount);
            st.setDouble(3, price);
            st.setInt(4, groupId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String readProduct(int id) {
        try {
            PreparedStatement st = con.prepareStatement("SELECT title, amount, price, group_id FROM product WHERE id = ?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return "Title: " + rs.getString("title") + ", Amount: " + rs.getInt("amount") + ", Price: " + rs.getDouble("price") + ", Group ID: " + rs.getInt("group_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProduct(int id, String newTitle, int newAmount, double newPrice, int newGroupId) {
        try {
            PreparedStatement st = con.prepareStatement("UPDATE product SET title = ?, amount = ?, price = ?, group_id = ? WHERE id = ?");
            st.setString(1, newTitle);
            st.setInt(2, newAmount);
            st.setDouble(3, newPrice);
            st.setInt(4, newGroupId);
            st.setInt(5, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int id) {
        try {
            PreparedStatement st = con.prepareStatement("DELETE FROM product WHERE id = ?");
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
/*
    public List<Product> getProductsAbovePrice(double priceThreshold) {
        try (Session session = HibernateUtil.getHibernateSession();
             CriteriaBuilder cb = session.getCriteriaBuilder();
             CriteriaQuery<Product> cr = cb.createQuery(Product.class);
             Root<Product> root = cr.from(Product.class);
             cr.select(root);

             Query<Product> query = session.createQuery(cr);
             List<Product> result = query.getResultList();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/



    public void createProductGroup(String title) {
        try {
            PreparedStatement st = con.prepareStatement("INSERT INTO product_group(title) VALUES(?)");
            st.setString(1, title);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String readProductGroup(int id) {
        try {
            PreparedStatement st = con.prepareStatement("SELECT title FROM product_group WHERE id = ?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProductGroup(int id, String newTitle) {
        try {
            PreparedStatement st = con.prepareStatement("UPDATE product_group SET title = ? WHERE id = ?");
            st.setString(1, newTitle);
            st.setInt(2, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProductGroup(int id) {
        try {
            PreparedStatement st = con.prepareStatement("DELETE FROM product_group WHERE id = ?");
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> listProductGroups() {
        List<String> items = new ArrayList<>();
        try {
            PreparedStatement st = con.prepareStatement("SELECT title FROM product_group");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                items.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void main(String[] args){
        DB sql = new DB();
        sql.initialization("productsDB");

        sql.createProductGroup("Electronics");
        sql.createProductGroup("Clothing");
        System.out.println("ProductGroup with id 1: " + sql.readProductGroup(1));
        sql.updateProductGroup(1, "Updated Electronics");
        sql.deleteProductGroup(2);
        List<String> productGroups = sql.listProductGroups();
        System.out.println("Product Groups: " + productGroups);

        sql.createProduct("Product 1", 10, 99.99, 1);
        sql.createProduct("Product 2", 5, 49.99, 1);
        System.out.println("Product with id 1: " + sql.readProduct(1));
        sql.updateProduct(1, "Updated Product 1", 15, 109.99, 1);
        sql.deleteProduct(2);
       //List<Product> productsAbovePrice = sql.getProductsAbovePrice(50.0);
        //System.out.println("Products above $50: " + productsAbovePrice);

    }
}
