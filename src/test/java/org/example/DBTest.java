package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class DBTest {
    private static DB db;

    @BeforeAll
    public static void setup() {
        db = new DB();
        db.initialization("testDB");
    }

    @Test
    public void testAllDatabaseOperations() {
        // Create a product group
        db.createProductGroup("Electronics");
        String group = db.readProductGroup(1);
        assertEquals("Electronics", group);

        // Update the product group
        db.updateProductGroup(1, "Updated Electronics");
        group = db.readProductGroup(1);
        assertEquals("Updated Electronics", group);

        // Create another product group
        db.createProductGroup("Clothing");

        // Delete a product group
        db.deleteProductGroup(2);
        group = db.readProductGroup(2);
        assertNull(group);

        // List all product groups
        db.createProductGroup("Sports");
        db.createProductGroup("Furniture");
        List<String> groups = db.listProductGroups();
        assertEquals(3, groups.size());
        assertTrue(groups.contains("Updated Electronics"));
        assertTrue(groups.contains("Sports"));
        assertTrue(groups.contains("Furniture"));

        // Create a product
        db.createProduct("Laptop", 10, 999.99, 1);
        String product = db.readProduct(1);
        assertEquals("Title: Laptop, Amount: 10, Price: 999.99, Group ID: 1", product);

        // Update the product
        db.updateProduct(1, "Updated Laptop", 15, 1099.99, 1);
        product = db.readProduct(1);
        assertEquals("Title: Updated Laptop, Amount: 15, Price: 1099.99, Group ID: 1", product);

        // Delete the product
        db.deleteProduct(1);
        product = db.readProduct(1);
        assertNull(product);
    }


}
