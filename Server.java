import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * MedBot - answers questions using passages from book.txt
 * (your own text copy of "The Gale Encyclopedia of Medicine").
 * No libraries needed, only the JDK.
 */
public class Server {

    static final int WINDOW = 140;   // words per passage
    static final int STEP = 110;     // overlap between passages
    static final Set<String> STOP = new HashSet<>(Arrays.asList(
        "what", "which", "who", "whom", "whose", "when", "where", "why", "how", "the", "and",
        "are", "was", "were", "for", "with", "about", "from", "that", "this", "these", "those",
        "does", "did", "can", "could", "should", "would", "tell", "give", "explain", "your",
        "into", "have", "has", "had", "its", "they", "them", "their", "there", "than", "then"));

    static List<String> passages = new ArrayList<>();
    static List<Map<String, Integer>> termCounts = new ArrayList<>();
    static Map<String, Integer> docFreq = new HashMap<>();

    public static void main(String[] args) throws Exception {
        loadBook(Paths.get("book.txt"));
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", Server::serveHome);
        server.createContext("/ask", Server::answer);
        server.start();
        System.out.println("MedBot is awake -> open http://localhost:8080");
    }

    // ---------- reading the book ----------
    static void loadBook(Path file) throws IOException {
        if (!Files.exists(file)) {
            System.out.println("!! book.txt not found. Put the book text next to Server.java");
            return;
        }
        String[] words = Files.readString(file, StandardCharsets.UTF_8).trim().split("\\s+");
        for (int i = 0; i < words.length; i += STEP) {
            int end = Math.min(words.length, i + WINDOW);
            String chunk = String.join(" ", Arrays.copyOfRange(words, i, end));
            Map<String, Integer> counts = new HashMap<>();
            for (String t : tokens(chunk)) counts.merge(t, 1, Integer::sum);
            for (String t : counts.keySet()) docFreq.merge(t, 1, Integer::sum);
            passages.add(chunk);
            termCounts.add(counts);
            if (end == words.length) break;
        }
        System.out.println("Read " + passages.size() + " passages from the book.");
    }

    static List<String> tokens(String text) {
        List<String> out = new ArrayList<>();
        for (String w : text.toLowerCase().split("[^a-z]+")) {
            if (w.length() > 2 && !STOP.contains(w)) out.add(w);
        }
        return out;
    }

    // ---------- answering ----------
    static void answer(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getRawQuery();
        String q = "";
        if (query != null && query.startsWith("q=")) q = URLDecoder.decode(query.substring(2), "UTF-8");

        List<String> qTerms = tokens(q);
        int n = passages.size();
        double[] score = new double[n];
        for (int i = 0; i < n; i++) {
            for (String t : qTerms) {
                Integer tf = termCounts.get(i).get(t);
                if (tf != null) score[i] += tf * Math.log(1 + (double) n / docFreq.get(t));
            }
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Double.compare(score[b], score[a]));

        StringBuilder json = new StringBuilder("{\"passages\":[");
        int added = 0;
        for (int k = 0; k < Math.min(3, n); k++) {
            if (score[order[k]] <= 0) break;
            if (added++ > 0) json.append(",");
            json.append("\"").append(esc(passages.get(order[k]))).append("\"");
        }
        json.append("]}");
        send(ex, 200, "application/json; charset=utf-8", json.toString().getBytes(StandardCharsets.UTF_8));
    }

    static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    // ---------- serving the page ----------
    static void serveHome(HttpExchange ex) throws IOException {
        Path page = Paths.get("public", "index.html");
        if (!Files.exists(page)) { send(ex, 404, "text/plain", "index.html missing".getBytes()); return; }
        send(ex, 200, "text/html; charset=utf-8", Files.readAllBytes(page));
    }

    static void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
}
