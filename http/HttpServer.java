package http;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpServer {

    private static final int PORT = ConfigLoader.configurationPort(); // Port du serveur
    private static final String BASE_PATH = ConfigLoader.configurationSource(); // Répertoire de base à servir
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Serveur démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleClientRequest(clientSocket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws Exception {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            System.out.println("Requête : " + requestLine);

            if (requestLine == null) {
                serve400(out);
                return;
            }

            String[] parts = requestLine.split(" ");
            String method = parts[0];  // GET ou POST
            String requestedPath = (parts.length > 1) ? parts[1] : "/";
            String cleVals = urlParser(requestedPath)[1];
            requestedPath = urlParser(requestedPath)[0];
            requestedPath = URLDecoder.decode(requestedPath, StandardCharsets.UTF_8.toString());

            System.out.println("Chemin demandé : " + requestedPath);

            if (method.equals("GET")) {
                handleGetRequest(requestedPath, cleVals, requestLine, out);
            } else if (method.equals("POST")) {
                handlePostRequest(requestedPath, requestLine, in, out);
            } else {
                serve400(out);
            }

        } finally {
            clientSocket.close();
        }
    }

    private static void handleGetRequest(String requestedPath, String data, String header, OutputStream out) throws Exception {
        // Nettoyer le chemin pour éviter les traversées de répertoire non sécurisées
        requestedPath = sanitizePath(requestedPath);
        StringBuilder sb = new StringBuilder(data);
        
        File file = new File(BASE_PATH + requestedPath);

        if (file.exists()) {
            if (file.isDirectory()) {
                if (!requestedPath.endsWith("/")) {
                    redirectToDirectory(out, requestedPath + "/");
                    return;
                }
                
                // Rechercher un fichier index
                File indexFile = findIndexFile(file);
                if (indexFile != null) {
                    serveFile(indexFile, out);
                } else {
                    // Si pas de fichier index, lister le contenu du répertoire
                    serveDirectoryListing(file, requestedPath, out);
                }
            } else {
                if (file.exists() && file.getName().endsWith(".php")) {
                    if (ConfigLoader.isPhpEnabled()) {
                        // Passer la méthode POST et les données
                        File f = PhpExecutor.phpExecutor(file, header, sb);
                        serveFile(f, out);
                        f.delete();
                    }
                } else {
                    serveFile(file, out);
                }    
            }
                // Servir le fichier demandé
        } else {
            serve404(out);
        }
    }

    // Méthode pour nettoyer et sécuriser le chemin
    private static String sanitizePath(String path) {
        // Normaliser le chemin pour éviter les traversées de répertoire
        path = path.replaceAll("\\.\\.(/|$)", "");
        
        // S'assurer que le chemin commence par un /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return path;
    }

    private static void handlePostRequest(String requestedPath, String header, BufferedReader in, OutputStream out) throws Exception {
        StringBuilder payload = new StringBuilder();
        String line;
        int contentLength = -1;
        String contentType = null;
    
        // Lire les en-têtes HTTP jusqu'à une ligne vide
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            // System.out.println("Header : " + line);
            
            // Extraire la longueur du contenu
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
            
            // Extraire le type de contenu
            if (line.toLowerCase().startsWith("content-type:")) {
                contentType = line.split(":")[1].trim();
            }
        }
    
        // Lire le corps de la requête
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            int charsRead = in.read(buffer, 0, contentLength);
            
            if (charsRead > 0) {
                payload.append(buffer, 0, charsRead);
            }
        } else {
            // Fallback si la longueur du contenu n'est pas spécifiée
            while (in.ready() && (line = in.readLine()) != null) {
                payload.append(line);
            }
        }
    
        System.out.println("Données POST reçues : " + payload.toString());
    
        File file = new File(BASE_PATH + requestedPath);
        if (file.exists() && file.getName().endsWith(".php")) {
            if (ConfigLoader.isPhpEnabled()) {
                // Passer la méthode POST et les données
                File f = PhpExecutor.phpExecutor(file, header, payload);
                serveFile(f, out);
                f.delete();
            }
        } else {
            serve404(out);
        }
    }
    

    private static void serveFile(File file, OutputStream out) throws Exception {
        if (!file.exists() || !file.isFile()) {
            serve404(out);
            return;
        }

        byte[] fileContent = Files.readAllBytes(file.toPath());
        String contentType = determineContentType(file);
        
        // Construction de l'en-tête HTTP
        StringBuilder responseHeaders = new StringBuilder();
        responseHeaders.append("HTTP/1.1 200 OK\r\n");
        responseHeaders.append("Content-Type: ").append(contentType).append("\r\n");
        responseHeaders.append("Content-Length: ").append(fileContent.length).append("\r\n");
        responseHeaders.append("\r\n");

        // Combiner les en-têtes et le contenu du fichier
        byte[] fullResponse = new byte[responseHeaders.length() + fileContent.length];
        System.arraycopy(responseHeaders.toString().getBytes(StandardCharsets.UTF_8), 0, fullResponse, 0, responseHeaders.length());
        System.arraycopy(fileContent, 0, fullResponse, responseHeaders.length(), fileContent.length);

        out.write(fullResponse);
        out.flush();
    }

    private static String determineContentType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        } else if (fileName.endsWith(".php")) {
            return "text/html; charset=UTF-8";
        } else if (fileName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "application/octet-stream";
        }
    }

    private static File findIndexFile(File directory) {
        String[] indexFiles = { "index.html", "index.htm", "index.php" };
        for (String indexFileName : indexFiles) {
            File indexFile = new File(directory, indexFileName);
            if (indexFile.exists() && indexFile.isFile()) {
                return indexFile;
            }
        }
        return null;
    }

    private static void serveDirectoryListing(File directory, String requestedPath, OutputStream out) throws IOException {
        File[] files = directory.listFiles();
    
        // Construction de la réponse HTML pour le listing des fichiers
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 200 OK\r\n");
            responseBuilder.append("Content-Type: text/html; charset=UTF-8\r\n\r\n");
            responseBuilder.append("<!DOCTYPE html>\n");
            responseBuilder.append("<html><head>");
            responseBuilder.append("<title>Index of " + requestedPath + "</title>");
            responseBuilder.append("<style>");
            responseBuilder.append("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }");
            responseBuilder.append("h1 { color: #333; }");
            responseBuilder.append("ul { list-style-type: none; padding: 0; }");
            responseBuilder.append("li { margin: 10px 0; padding: 10px; background-color: #f4f4f4; border-radius: 5px; display: flex; align-items: center; }");
            responseBuilder.append("a { text-decoration: none; color: #0066cc; margin-left: 10px; }");
            responseBuilder.append("a:hover { text-decoration: underline; }");
            responseBuilder.append("img { max-width: 20px; max-height: 20px; }");
            responseBuilder.append("</style>");
            responseBuilder.append("</head><body>");
            responseBuilder.append("<h1>Index of " + requestedPath + "</h1>");
        responseBuilder.append("<ul>");
    
        // Ajouter un lien vers le répertoire parent si ce n'est pas la racine
        if (!requestedPath.equals("/")) {
            responseBuilder.append("<li><a href=\"../\"><img src=\"/icons/return.png\" alt=\"Folder\"> ../</a> (Parent Directory)</li>");
        }
    
        // Trier les fichiers et dossiers
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                // Les dossiers en premier, puis triés alphabétiquement
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareTo(f2.getName());
            });
    
            // Ajouter chaque fichier et dossier
            for (File file : files) {
                String fileName = file.getName();
                String filePath = requestedPath.endsWith("/") ? requestedPath + fileName : requestedPath + "/" + fileName;
    
                responseBuilder.append("<li>");
    
                // Ajouter l'icône en fonction du type (dossier ou fichier)
                if (file.isDirectory()) {
                    responseBuilder.append("<img src=\"/icons/folder.png\" alt=\"Folder\">");
                } else {
                    responseBuilder.append("<img src=\"/icons/text.png\" alt=\"File\">");
                }
    
                // Ajouter le lien
                responseBuilder.append("<a href=\"" + filePath + (file.isDirectory() ? "/" : "") + "\">" + fileName + (file.isDirectory() ? "/" : "") + "</a>");
    
                // Ajouter des informations supplémentaires pour les fichiers
                if (file.isFile()) {
                    responseBuilder.append(" <small>(").append(file.length()).append(" bytes)</small>");
                }
    
                responseBuilder.append("</li>");
            }
        }
    
        responseBuilder.append("</ul>");
        responseBuilder.append("<hr><small>Simple Java HTTP Server</small>");
        responseBuilder.append("</body></html>");
    
        // Envoyer la réponse
        String response = responseBuilder.toString();
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }
    
    private static void serve404(OutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                          "Content-Type: text/html; charset=UTF-8\r\n" +
                          "\r\n" +
                          "<!DOCTYPE html><html><body><h1>404 Not Found</h1><p>Le fichier demandé n'existe pas.</p></body></html>";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static void serve400(OutputStream out) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                          "Content-Type: text/html; charset=UTF-8\r\n" +
                          "\r\n" +
                          "<!DOCTYPE html><html><body><h1>400 Bad Request</h1><p>Requête incorrecte.</p></body></html>";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static void redirectToDirectory(OutputStream out, String location) throws IOException {
        String response = "HTTP/1.1 301 Moved Permanently\r\n" +
                          "Location: " + location + "\r\n" +
                          "Content-Length: 0\r\n" +
                          "\r\n";
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static String[] urlParser(String requestPath){
        String[] urls = new String[2];
        urls[0] = requestPath;
        urls[1] = "";
        if (requestPath.contains("?")) {
            urls[0] = requestPath.substring(0, requestPath.indexOf("?"));
            urls[1] = requestPath.substring(requestPath.indexOf("?")+1);
        }
        return urls;
    }

}