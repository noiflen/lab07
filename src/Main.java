import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Подключение к базе данных установлено.");

            setupDatabase(conn);
            task1(conn);
            task2(conn);
            task3(conn);
            task4(conn);
            task5(conn);
            task6(conn);
            task7(conn);
            task8(conn);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupDatabase(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS study");
        }

        // Выполняем скрипт из файла, если он есть
        File sqlFile = new File("music-create.sql");
        if (sqlFile.exists()) {
            String sql = new String(Files.readAllBytes(sqlFile.toPath()));
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } else {
            // Резервное создание таблицы, если файла нет
            String createMusicTable = "CREATE TABLE IF NOT EXISTS study.music (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) NOT NULL)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createMusicTable);
                stmt.execute("INSERT INTO study.music (name) VALUES ('Moonlight Sonata'), ('Symphony No 5'), ('Happy Tree Friends')");
            }
        }
    }

    private static void task1(Connection conn) throws SQLException {
        System.out.println("Задание 1: Список музыкальных композиций");
        String sql = "SELECT * FROM study.music";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Название: " + rs.getString("name"));
            }
        }
    }

    private static void task2(Connection conn) throws SQLException {
        System.out.println("2: Композиции без букв 'm' и 't' (без учёта регистра)");
        String sql = "SELECT * FROM study.music WHERE LOWER(name) NOT LIKE '%m%' AND LOWER(name) NOT LIKE '%t%'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Название: " + rs.getString("name"));
            }
        }
    }

    private static void task3(Connection conn) throws SQLException {
        System.out.println("\n--- Задание 3: Добавление любимой композиции ---");
        String sql = "INSERT INTO study.music (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Моя любимая композиция");
            System.out.println("Добавлено строк: " + ps.executeUpdate());
        }
    }

    private static void task4(Connection conn) throws Exception {
        System.out.println("\n--- Задание 4: Загрузка данных из JSON (только уникальные) ---");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS visitors (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS books (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) UNIQUE, publish_year INT)");
        }

        File jsonFile = new File("books.json");
        JSONArray jsonArray;
        if (jsonFile.exists()) {
            String jsonText = new String(Files.readAllBytes(jsonFile.toPath()));
            jsonArray = new JSONArray(jsonText);
        } else {
            System.out.println("⚠️ Файл books.json не найден. Используются тестовые данные.");
            jsonArray = new JSONArray("[{\"visitor\": \"Алексей\", \"book_title\": \"Мастер и Маргарита\", \"publish_year\": 1967}, {\"visitor\": \"Мария\", \"book_title\": \"1984\", \"publish_year\": 1949}]");
        }

        // Используем INSERT ... SELECT WHERE NOT EXISTS для гарантии уникальности без дубликатов
        String insertVisitor = "INSERT INTO visitors (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM visitors WHERE name = ?)";
        String insertBook = "INSERT INTO books (title, publish_year) SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = ?)";

        try (PreparedStatement psVisitor = conn.prepareStatement(insertVisitor);
             PreparedStatement psBook = conn.prepareStatement(insertBook)) {

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String visitor = obj.getString("visitor");
                String bookTitle = obj.getString("book_title");
                int year = obj.getInt("publish_year");

                psVisitor.setString(1, visitor);
                psVisitor.setString(2, visitor);
                psVisitor.executeUpdate();

                psBook.setString(1, bookTitle);
                psBook.setInt(2, year);
                psBook.setString(3, bookTitle);
                psBook.executeUpdate();
            }
        }
        System.out.println(" Данные из JSON  загружены (дубликаты проигнорированы).");
    }

    private static void task5(Connection conn) throws SQLException {
        System.out.println("Задание 5: Сортировка книг по году издания ");
        String sql = "SELECT * FROM books ORDER BY publish_year ASC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("Книга: " + rs.getString("title") + ", Год: " + rs.getInt("publish_year"));
            }
        }
    }

    private static void task6(Connection conn) throws SQLException {
        System.out.println("\n--- Задание 6: Книги, изданные до 2000 года ---");
        String sql = "SELECT * FROM books WHERE publish_year < 2000";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("Книга: " + rs.getString("title") + ", Год: " + rs.getInt("publish_year"));
            }
        }
    }

    private static void task7(Connection conn) throws SQLException {
        System.out.println("Задание 7: Добавление себя и своих любимых книг");

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO visitors (name) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM visitors WHERE name = ?)")) {
            ps.setString(1, "Иван Студентов"); // ЗАМЕНИТЕ НА СВОЁ ИМЯ
            ps.setString(2, "Иван Студентов");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO books (title, publish_year) SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM books WHERE title = ?)")) {
            ps.setString(1, "Чистый код"); // ЗАМЕНИТЕ НА НАЗВАНИЕ КНИГИ
            ps.setInt(2, 2008);            // ЗАМЕНИТЕ НА ГОД
            ps.setString(3, "Чистый код");
            ps.executeUpdate();
        }

        System.out.println("Добавленные посетители:");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM visitors")) {
            while(rs.next()) System.out.println("- " + rs.getString("name"));
        }

        System.out.println("Добавленные книги:");
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            while(rs.next()) System.out.println("- " + rs.getString("title") + " (" + rs.getInt("publish_year") + ")");
        }
    }

    private static void task8(Connection conn) throws SQLException {
        System.out.println("Задание 8: Удаление таблиц");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS books");
            stmt.execute("DROP TABLE IF EXISTS visitors");
            System.out.println("Таблицы visitors и books успешно удалены.");
        }
    }
}
