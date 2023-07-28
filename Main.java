
/**
 * Jeu du pendu en java
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileReader;
import java.io.FileWriter;

public class Main {
    public static String getMotAleatoire(String path) {
        String mot = "";
        try {
            // Chemin vers le fichier contenant les mots
            String filePath = path;

            // Lire le fichier ligne par ligne
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line;
            ArrayList<String> listeDeMots = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                // Ajouter chaque mot à la liste
                listeDeMots.add(line.trim());
            }

            reader.close();
            // Sélectionner un mot aléatoire pour le jeu de pendu
            Random random = new Random();
            int indexMotAleatoire = random.nextInt(listeDeMots.size());
            mot = listeDeMots.get(indexMotAleatoire);
        } catch (IOException e) {
            throw new Error("Erreur lors de la lecture du fichier : " + e.getMessage());
        }
        return mot;
    }

    public static void main(String[] args) {
        // Initialisation des variables
        Scanner sc = new Scanner(System.in, "UTF-8");
        int reJouer = 0;
        String lettresJouees = "";
        String motCache = "";
        int nbCoups = 0;
        int nbCoupsMax = 10;
        char lettre = ' ';
        boolean trouve = false;

        // on cree un menu dans une boucle do while
        int jouer = afficherMenu(sc, new String[] {
                "1. Jouer",
                "2. Quitter",
        }, new String[] {
                "Vous avez choisi de jouer",
                "Vous avez choisi de quitter",
        });
        if (jouer == 2) {
            System.exit(0);
        }

        // on demande a l'utilisateur de saisir son pseudo
        print("Veuillez saisir votre pseudo : ");
        String pseudo = sc.nextLine();
        ajouterJoueur(pseudo, "scores.csv");
        sc.nextLine();

        do {

            lettresJouees = "";
            motCache = "";
            nbCoups = 0;
            nbCoupsMax = 10;
            lettre = ' ';
            trouve = false;

            // on cree une boucle de selection de dificulté
            int dificulte = afficherMenu(sc, new String[] {
                    "1. Debutant",
                    "2. Intermediaire",
                    "3. Expert"
            }, new String[] {
                    "Vous avez choisi la dificulte debutant",
                    "Vous avez choisi la dificulte intermediaire",
                    "Vous avez choisi la dificulte expert"
            });
            String mot = getMotAleatoire("mot" + dificulte + ".txt");

            // On cache le mot
            for (int i = 0; i < mot.length(); i++) {
                motCache += "_";
            }

            // On demande à l'utilisateur de saisir une lettre
            while (nbCoups < nbCoupsMax && !trouve) {
                // on reinitialise l'affichage
                clear();
                print(motCache);
                afficherPendu(nbCoups);
                print("Il vous reste " + (nbCoupsMax - nbCoups) + " coups à jouer.");
                print("Lettres déjà jouées : " + lettresJouees);
                // On récupère la lettre saisie par l'utilisateur, si l'utilisateur ne saisit
                // pas une lettre, on lui demande de saisir une lettre
                while (true) {
                    print("Veuillez saisir une lettre : ");
                    lettre = sc.next().toLowerCase().charAt(0);
                    sc.nextLine();
                    if (Character.isAlphabetic(lettre)
                            && !lettresJouees.contains(String.valueOf(lettre).toLowerCase())) {
                        // La lettre est valide et non encore jouée
                        break; // Sort de la boucle while
                    } else {
                        print("Lettre invalide ou déjà jouée. Veuillez réessayer.");
                    }
                }

                // On parcourt le mot pour voir si la lettre saisie est dedans
                boolean lettreTrouvee = false;
                for (int i = 0; i < mot.length(); i++) {
                    if (lettre == mot.charAt(i)) {
                        motCache = motCache.substring(0, i) + lettre + motCache.substring(i + 1, motCache.length());
                        lettreTrouvee = true;
                    }
                }
                // On ajoute la lettre aux lettres jouées
                lettresJouees += (lettresJouees.length() > 0 ? "," : "") + lettre;
                if (!lettreTrouvee) {
                    nbCoups++;
                }

                // On affiche le mot avec les lettres trouvées

                // On vérifie si le mot est trouvé
                if (motCache.equals(mot)) {
                    trouve = true;
                }
            }

            if (trouve) {
                clear();
                print(motCache);
                afficherPendu(10);

            } else {
                print("Vous avez perdu ! Le mot était : " + mot);
            }

            modifyPlayerData(pseudo, nbCoupsMax - nbCoups);

            // on met le jeu en pause
            print("Appuyez sur entrée pour continuer...");
            sc.nextLine();

            reJouer = afficherMenu(sc, new String[] {
                    "1. re Jouer",
                    "2. Quitter",
            }, new String[] {
                    "Vous avez choisi de rejouer",
                    "Vous avez choisi de quitter",
            });

        } while (reJouer == 1);

        sc.close();
    }

    public static void modifyPlayerData(String playerName, int newScore) {
        try {
            List<String> lines = new ArrayList<>();
            boolean playerFound = false;

            try (BufferedReader reader = new BufferedReader(new FileReader("scores.csv"))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] row = line.split(";");
                    String pseudo = row[0].replaceAll("\"", "");

                    if (pseudo.equals(playerName)) {
                        playerFound = true;
                        int score = Integer.parseInt(row[1].replaceAll("\"", ""));
                        int totalScore = score + newScore;
                        row[1] = "\"" + totalScore + "\"";
                        int scoreMax = Integer.parseInt(row[2].replaceAll("\"", ""));
                        if (newScore > scoreMax) {
                            row[2] = "\"" + newScore + "\"";
                        }
                        int playedGames = Integer.parseInt(row[3].replaceAll("\"", "")) + 1;
                        row[3] = "\"" + playedGames + "\"";
                        float ratio = (float) totalScore / playedGames * 10;
                        row[4] = "\"" + (int) ratio + "%\"";
                        line = String.join(";", row);
                    }
                    lines.add(line);
                }
            }

            if (!playerFound) {
                System.out.println("Player " + playerName + " not found in CSV.");
            } else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.csv"))) {
                    for (String line : lines) {
                        writer.write(line);
                        if (!line.equals(lines.get(lines.size() - 1))) {
                            writer.newLine();
                        }
                    }
                    System.out.println("Player " + playerName + " Data updated.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPlayerPresent(String playerName, String csvFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header line
                }

                String[] row = line.split(";");

                if (row.length > 0) {
                    String pseudo = row[0].replaceAll("\"", "");
                    if (pseudo.equals(playerName)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void ajouterJoueur(String playerName, String csvFilePath) {
        if (!isPlayerPresent(playerName, csvFilePath)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, true))) {
                String newPlayerRow = "\"" + playerName + "\";\"0\";\"0\";\"0\";\"0\"";
                writer.newLine();
                writer.write(newPlayerRow);
                System.out.println("Player " + playerName + " added to CSV.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        print("Bienvenue " + playerName + " !");
    }

    public static void print(String message) {
        System.out.println(message);
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
    }

    public static int afficherMenu(Scanner sc, String[] options, String[] messages) {
        int choix = 0;
        do {
            clear();
            for (int i = 0; i < options.length; i++) {
                print(options[i]);
            }
            print("Votre choix : ");
            choix = sc.nextInt();
            sc.nextLine();
            for (int i = 0; i < options.length; i++) {
                if (choix == i + 1) {
                    print(messages[i]);
                }
            }
        } while (choix == 0 || choix > options.length);
        return choix;
    }

    public static void afficherPendu(int nbCoups) {
        // Motif ASCII du pendu en fonction du nombre de coups incorrects
        String[] pendu = {
                "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "       \n" +
                        "=========",
                "       \n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        "      |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        "  |   |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        " /|   |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        " /|\\  |\n" +
                        "      |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        " /|\\  |\n" +
                        " /    |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "  |   |\n" +
                        "  O   |\n" +
                        " /|\\  |\n" +
                        " / \\  |\n" +
                        "      |\n" +
                        "=========",
                "  +---+\n" +
                        "      |\n" +
                        "      |\n" +
                        " \\O/  |\n" +
                        "  |   |\n" +
                        " / \\  |\n" +
                        "========="
        };

        // Affiche le motif du pendu en fonction du nombre de coups incorrects
        if (nbCoups >= 0 && nbCoups < pendu.length) {
            print(pendu[nbCoups]);
        } else {
            print("Motif du pendu indisponible pour " + nbCoups + " coups incorrects.");
        }
    }
}