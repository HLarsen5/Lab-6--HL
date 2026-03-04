import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Functions {

    //NOTE: some basic code has been provided to help process the input file
    //PRE: inputFile is the name of the CSV file to read, errorFile is the name of the file to write invalid records to,
    //     shows is an (initially empty) ArrayList that will store valid Show objects.
    //POST: reads each record from inputFile, validates required fields and numeric fields,
    //      writes invalid records to errorFile, and appends valid Show objects into shows.
    public static void loadList(String inputFile, String errorFile, ArrayList<Show> shows) {
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            BufferedWriter errorWriter = new BufferedWriter(new FileWriter(errorFile))) {
            String header = reader.readLine(); // Skip header
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    ArrayList<String> tokens = parseCSVLine(line);

                    if (tokens.size() < 15) throw new IllegalArgumentException("Not enough tokens");

                    //trim all fields 0 id has been done for you
                    String id = tokens.get(0).trim();
                    String title = tokens.get(1).trim();
                    String type = tokens.get(2).trim();
                    String description = tokens.get(3).trim();

                    //verify that releaseYear is an integer
                    int releaseYear = parseInt(tokens.get(4).trim(), "release_year");
                    String rating = tokens.get(5).trim();

                    //runtime is not necessary
                    int runtime = parseOptionalInt(tokens.get(6).trim(), "runtime");

                    //parseList is provided here for genres. 
                    //Add necessary call for countries as well
                    ArrayList<String> genres = parseList(tokens.get(7));
                    ArrayList<String> countries = parseList(tokens.get(8));

                    //these functions need to be written
                    double seasons = parseOptionalDouble(tokens.get(9).trim(), "seasons");
                    double score = parseDouble(tokens.get(11).trim(), "imdb_score");

                    //throw error for missing genres or countries
                    if (genres.isEmpty()) {
                        throw new IllegalArgumentException("Missing genres");
                    }
                    if (countries.isEmpty()) {
                        throw new IllegalArgumentException("Missing countries");
                    }
                    

                    shows.add(new Show(id, title, type, description, releaseYear, rating, runtime, genres, countries, seasons, score));

                } catch (Exception e) {
                    errorWriter.write("Error: " + e.getMessage() + " — Line: " + line + "\n");
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    //NOTE: some basic code has been provided 
    //PRE: shows contains valid Show objects; reportFile is the filename to write to.
    //POST: writes two reports into reportFile:
    //      (1) 10 longest running TV shows (by season)
    //      (2) top 10 movies by IMDB score
    public static void printReport(ArrayList<Show> shows, String reportFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {

            writer.write("Top 10 Longest Running TV Shows:\n\n");
            
            //create a list of only TV Shows & only movies
            List<Show> tvShows  = new ArrayList<>();
            List<Show> movies  = new ArrayList<>();

            //add logic to create the movies list (tvShows has been provided)
            for (Show s: shows){
                if ("SHOW".equalsIgnoreCase(s.type)) {
                    tvShows.add(s);
                } else if ("MOVIE".equalsIgnoreCase(s.type)) {
                    movies.add(s);
                }    
            }

            //sort list in descending order
            tvShows.sort(Comparator.comparingDouble(s -> -s.seasons));
            int tvLimit = Math.min(10, tvShows.size());

            //create a sublist of the top 10 shows
            if (tvShows.size() > 10)
                tvShows = tvShows.subList(0,10);

            //print the tvShows
            writer.write("SHOWS WITH THE LONGEST DURATION");
            writer.newLine();
            writer.newLine();
            writer.write(String.format("%-45s %-8s %-10s %8s %-8s %-35s", "Title", "Year", "Rating", "Seasons", "Score", "Genres"));
            writer.newLine();

            for (int i = 0; i < tvLimit; i++) {
                Show s = tvShows.get(i);
                writer.write(String.format("%-45.45s %-8d %-10s %8.0f %-8.2f %-35.35s",
                s.title,
                s.releaseYear,
                (s.rating == null ? "" : s.rating),
                s.seasons,
                s.score,
                joinList(s.genres)));
                writer.newLine();
            }


            
            writer.write("\n\nTop 10 Movies by IMDB Score:\n\n");

            //add logic to handle the  movies & print top 10 best scores
            writer.write("MOVIES WITH THE BEST RATINGS");
            writer.newLine();
            writer.newLine();
            writer.write(String.format("%-45s %-6s %-8s %8s %-8s %-35s", "Title", "Year", "Rating", "Runtime", "Score", "Countries"));
            writer.newLine();

            movies.sort(Comparator.comparingDouble(s -> -s.score));
            int movieLimit = Math.min(10, movies.size());


            for (int i = 0; i < movieLimit; i++) {
                Show s = movies.get(i);
                writer.write(String.format("%-45.45s %-6d %-8.8s %8d %-8.2f %-35.35s",
                s.title,
                s.releaseYear,
                (s.rating == null ? "" : s.rating),
                s.runtime,
                s.score,
                joinList(s.countries)));
                writer.newLine();
            }
 

        } catch (IOException e) {
            System.err.println("Error writing report: " + e.getMessage());
        }
// used ChatGPT to help with printed list formatting
        
    }

    private static String joinList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return String.join(", ", list);
        }

    //PRE: s is a string representing an integer
    //POST: returns int value of s if valid and throws an exception if not
    private static int parseInt(String s, String field) throws Exception {
        if (s == null || s.isEmpty() || s.equalsIgnoreCase("NaN"))
            throw new NumberFormatException("Invalid " + field);
        return Integer.parseInt(s);
    }

    //PRE: s may be blank/ NaN or a valid integer string
    //POST: returns parsed int or ) if missing/ invalid
    private static int parseOptionalInt(String s, String field) {
        try {
            return parseInt(s, field);
        } catch (Exception e) {
            return 0; // fallback if optional
        }
    }

    //PRE: s is a string representing a double
    //POST: returns double value of s if valid and an exception if not
    private static double parseDouble(String s, String field) throws Exception {
        if (s == null || s.trim().isEmpty() || s.equalsIgnoreCase("NaN"))
            throw new NumberFormatException("Invalid " + field);
        return Double.parseDouble(s.trim());
    }

    //PRE: s may be blank/ NaN or a valid double string
    //POST: returns parsed double or 0.0 if missing/ invalid
    private static double parseOptionalDouble(String s, String field) {
        try {
            return parseDouble(s, field);
        } catch (Exception e) {
            return 0.0;
        }
    }

    //parseList has been provided
    //PRE: take a field that contains a sublist
    //POST return an arraylist of values from this list
    private static ArrayList<String> parseList(String s) {
        
        String cleaned = s.replaceAll("\\[|\\]|'", "").trim();
        ArrayList<String> result = new ArrayList<>();

        if (cleaned.isEmpty()) 
            return result;

        String[] items = cleaned.split(",");
        for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty())
                   result.add(trimmed);
        }
        return result;
    }

    //parseCSVLine has been provided
    //PRE:  input current input line
    //POST: return the list of tokens (some fields may have a sublist(sb)) 
    private static ArrayList<String> parseCSVLine(String line) {
        ArrayList<String> tokens = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;

    
    }
}

        tokens.add(sb.toString());
        return tokens;
    }
}
