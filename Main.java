
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
import java.util.Arrays;
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
        String lettresJouees;
        String motCache = "";
        int nbCoups;
        int nbCoupsMax = 10;
        char lettre;

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
        print("Veuillez saisir votre pseudo :");
        String pseudo = sc.nextLine();
        ajouterJoueur(pseudo, "scores.csv");

        int rejouer;
        do {
            // on reinitialise les variables
            lettresJouees = "";
            nbCoups = 0;

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
            motCache = hideLettresMot(mot);

            // On demande à l'utilisateur de saisir une lettre
            while (nbCoups < nbCoupsMax && !isMotTrouvee(motCache, mot)) {
                // on reinitialise l'affichage
                clear();
                println("Il vous reste " + (nbCoupsMax - nbCoups) + " coups à jouer");
                afficherPendu(nbCoups, lettresJouees);
                println("Le mot caché : " + motCache);
                // On récupère la lettre saisie par l'utilisateur, si l'utilisateur ne saisit
                // pas une lettre, on lui demande de saisir une lettre
                while (true) {
                    print("Veuillez saisir une lettre :");
                    lettre = sc.next().toLowerCase().charAt(0);
                    sc.nextLine();
                    if (Character.isAlphabetic(lettre) && !lettresJouees.contains(String.valueOf(lettre).toLowerCase())) {
                        // La lettre est valide et non encore jouée
                        break; // Sort de la boucle while
                    }
                }

                if (isLettreTrouvee(mot, lettre)) {
                    // On met a jour le mot caché
                    motCache = majMotCache(motCache, lettre, positionLettre(mot, lettre));
                } else {
                    // On ajoute la lettre aux lettres jouées
                    lettresJouees += lettre;
                    nbCoups++;
                }
            }

            if (isMotTrouvee(motCache, mot)) {
                clear();
                afficherPendu(nbCoupsMax, lettresJouees);
                println("Gagné : " + motCache);
            } else {
                println("Vous avez perdu ! Le mot était : " + mot);
            }

            modifyPlayerData(pseudo, nbCoupsMax - nbCoups);

            // on met le jeu en pause
            print("Appuyez sur entrée pour continuer...");
            sc.nextLine();

            rejouer = afficherMenu(sc, new String[] {
                    "1. Jouer (encore)",
                    "2. Quitter",
            }, new String[] {
                    "Vous avez choisi de rejouer",
                    "Vous avez choisi de quitter",
            });

        } while (rejouer == 1);

        sc.close();
    }

    private static String hideLettresMot(String mot) {
        String motCache = "";
        for (int i = 0; i < mot.length(); i++) {
            if (mot.charAt(i) == ' ') {
                motCache += " ";
            } else {
                motCache += "_";
            }
        }
        return motCache;
    }

    private static String majMotCache(String motCache, char lettre, int position) {
        return motCache.substring(0, position) + lettre + motCache.substring(position + 1, motCache.length());
    }

    private static boolean isMotTrouvee(String motcache, String mot) {
        return motcache.equals(mot);
    }

    private static boolean isLettreTrouvee(String mot, char lettre) {
        return positionLettre(mot, lettre) != -1;
    }

    private static int positionLettre(String mot, char lettre) {
        for (int i = 0; i < mot.length(); i++) {
            if (lettre == mot.charAt(i)) {
                return i;
            }
        }
        return -1;
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
                println("Player " + playerName + " not found in CSV.");
            } else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.csv"))) {
                    for (String line : lines) {
                        writer.write(line);
                        if (!line.equals(lines.get(lines.size() - 1))) {
                            writer.newLine();
                        }
                    }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void println(String message) {
        System.out.println(message);
    }

    private static void print(String message) {
        System.out.print(message);
    }

    public static void clear() {
        print("\033[H\033[2J");
    }

    public static int afficherMenu(Scanner sc, String[] options, String[] messages) {
        int choix = 0;
        do {
            clear();
            for (int i = 0; i < options.length; i++) {
                println(options[i]);
            }
            print("Votre choix :");
            choix = sc.nextInt();
            sc.nextLine();
            clear();
            if (choix > 0 && choix <= options.length) {
                println(messages[choix-1]);
            }
        } while (choix < 1 || choix > options.length);
        return choix;
    }

    public static void afficherPendu(int nbCoups, String lettresjouees) {
        // Plancher initiale
        String plancher = "==========";
        // trier les lettres dans l'ordre alphabetique
        String[] lettres = lettresjouees.split("");
        Arrays.sort(lettres);
        lettresjouees = String.join("", lettres);
        // Plancher remplacé par les lettres jouees
        plancher = lettresjouees + plancher.substring(lettresjouees.length());
        // Motif ASCII du pendu en fonction du nombre de coups incorrects
        String[] pendu = {
            "        \n" +
            "        \n" +
            "        \n" +
            "        \n" +
            "        \n" +
            "        \n",
            "        \n" +
            "       |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "       |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "       |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "   |   |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "  /|   |\n" +
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "  /|\\  |\n"+
            "       |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "  /|\\  |\n"+
            "  /    |\n" +
            "       |\n",
            "   +---+\n" +
            "   |   |\n" +
            "   O   |\n" +
            "  /|\\  |\n"+
            "  / \\  |\n"+
            "       |\n",
            "   +---+\n" +
            "       |\n" +
            "       |\n" +
            "  \\O/  |\n"+
            "   |   |\n" +
            "  / \\  |\n"
        };

        // Affiche le motif du pendu en fonction du nombre de coups incorrects
        if (nbCoups >= 0 && nbCoups < pendu.length) {
            println(pendu[nbCoups] + plancher);
        } else {
            println("Motif du pendu indisponible pour " + nbCoups + " coups incorrects.");
        }
    }
}