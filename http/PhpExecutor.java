package http;

import java.io.*;
import java.util.*;

public class PhpExecutor {

    public static File phpExecutor(File phpFile, String headerPath, StringBuilder payload)
            throws Exception, IOException {
        String[] headers = headerPath.split(" ");
        String cleValeurs = payload.toString();
        int i = phpFile.getAbsolutePath().lastIndexOf("/");
        System.out.println(phpFile.getAbsolutePath().substring(0, i) + "/temp.php");

        if (cleValeurs != null && !cleValeurs.trim().equals("")) {
            File temp = new File(phpFile.getAbsolutePath().substring(0, i) + "/temp.php");
            FileWriter fileWriter = new FileWriter(temp);
            FileReader reader = new FileReader(phpFile);

            if (headers[0].equals("GET")) {
                fileWriter.write("<?php\r\n parse_str(implode('&', array_slice($argv, 1)), $_GET);\r\n ?>");
            } else {
                fileWriter.write("<?php\r\n parse_str(implode('&', array_slice($argv, 1)), $_POST);\r\n ?>");
            }
            //copier le contenue du fichier demandee dans le fichier temp.php
            reader.transferTo(fileWriter); 

            fileWriter.close();
            reader.close();
            //compile le fichier temp.php et passer les donnees vers temp.php
            ProcessBuilder processBuilder = new ProcessBuilder("php", temp.getAbsolutePath(), cleValeurs);

            File output = new File(phpFile.getAbsolutePath().substring(0, i) +"/temp.html"); //creer un nouveau fichier temp.html et y passer les donnees 
            processBuilder.redirectErrorStream(true); // Redirige la sortie d'erreur vers la sortie standard ********
            processBuilder.redirectOutput(output); // Rediriger la sortie standard vers un fichier 

            // Démarrer le processus
            Process process = processBuilder.start();
            // Attendre que le processus se termine et récupérer son code de sortie
            int exitCode = process.waitFor();
            temp.delete(); //temp.php
            if (exitCode == 0) {
                return output;
            }
            throw new Exception("Le processus a terminé avec le code de sortie : " + exitCode);
        } else {
            // Chemin du fichier de sortie pour enregistrer les résultats
            String outputFilePath = "temp.html";

            // Construire la commande PHP pour exécuter le fichier PHP
            List<String> command = new ArrayList<>();
            command.add("php");// Ou le chemin absolu de php, comme "C:/php/php.exe"
            command.add(phpFile.getAbsolutePath());

            try {
                // Créer le processus pour exécuter PHP
                File output = new File(outputFilePath);
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // Redirige la sortie d'erreur vers la sortie standard
                processBuilder.redirectOutput(output); // Rediriger la sortie standard vers un fichier

                // Démarrer le processus
                Process process = processBuilder.start();

                // Attendre que le processus se termine et récupérer son code de sortie
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return output;
                }
                throw new Exception("Le processus a terminé avec le code de sortie : " + exitCode);
            } catch (IOException | InterruptedException e) {
                throw e;
            }
        }
    }

    public static String cleValeurs(String methode, String headHttp, BufferedReader read) throws Exception {
        String valiny = "";
        if (methode.equalsIgnoreCase("GET")) {
            if (headHttp.contains("?")) {
                return headHttp.split(" ")[1].substring(headHttp.split(" ")[1].indexOf("?") + 1);
            }
            return null;
        }
        if (methode.equalsIgnoreCase("POST")) {
            String request;
            StringBuilder body = new StringBuilder();
            System.out.println(read.readLine());
            // while ((request = read.readLine()) != null && !request.isEmpty()) {

            // }
            while (read.ready()) {
                body.append((char) read.read());
            }
            valiny = body.toString();
        }
        return valiny;
    }
}