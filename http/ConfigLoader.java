package http;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static int configurationPort() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("configuration.properties")) {
            properties.load(input);
            int port = Integer.parseInt(properties.getProperty("port"));
            
            System.out.println("Server running on :" + port);
            return port;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String configurationSource() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("configuration.properties")) {
            properties.load(input);
            String host = properties.getProperty("src");
            
            System.out.println("Server running on " + host);
            return host;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isPhpEnabled() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("configuration.properties")) {
            properties.load(input);
            String phpEnabled = properties.getProperty("statut_php");
            
            boolean isEnabled = Boolean.parseBoolean(phpEnabled);
            System.out.println("Le support PHP est " + (isEnabled ? "activé" : "désactivé"));
            return isEnabled;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // par défaut, PHP est désactivé si la configuration n'est pas trouvée
    }
}

