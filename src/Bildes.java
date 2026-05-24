import java.io.*;
import java.net.*;
import java.sql.*;
import java.nio.file.*;

public class Bildes {

    public static void main(String[] args) throws Exception {
        
        Path imagesDir = Paths.get("images");
        Files.createDirectories(imagesDir);
        System.out.println("Mape 'images/' izveidota.");

        Database db = new Database();
        ResultSet rs = db.getConn().createStatement()
            .executeQuery("SELECT id, nosaukums, foto FROM preces WHERE foto IS NOT NULL AND foto != ''");

        while (rs.next()) {
            int id       = rs.getInt("id");
            String url   = rs.getString("foto").trim();
            String name  = rs.getString("nosaukums")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")  
                .replaceAll("_+", "_")          
                .replaceAll("^_|_$", "");       
            String fileName = name + ".jpg";
            Path outPath = imagesDir.resolve(fileName);

            System.out.print("Lejupielādē #" + id + ": " + url.substring(0, Math.min(60, url.length())) + "... ");

            try {
                URLConnection conn = new URL(url).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                conn.setRequestProperty("Referer", "https://www.google.com");

                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                }

                long size = Files.size(outPath);
                if (size < 500) {
                    Files.delete(outPath);
                    System.out.println("BLOĶĒTS (fails par mazu)");
                } else {
                    
                    PreparedStatement upd = db.getConn().prepareStatement(
                        "UPDATE preces SET foto = ? WHERE id = ?");
                    upd.setString(1, "images/" + fileName);
                    upd.setInt(2, id);
                    upd.executeUpdate();
                    System.out.println("OK (" + size/1024 + " KB) → " + fileName);
                }

            } catch (Exception e) {
                System.out.println("KĻŪDA: " + e.getMessage());
            }
        }

        System.out.println("\nGatavs! Tagad palaid Main.java");
    }
}