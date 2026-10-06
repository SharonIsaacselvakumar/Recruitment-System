package recruitment.backend;

import java.io.*;
import java.util.*;

/** Backend: data store, persistence and all validation rules. */
public class Service {
    public static final String[] ROLES = {"Candidate", "HR Manager", "Administrator"};
    public static final String[] LEVELS = {"Diploma", "Bachelor's", "Master's"};
    public static final String[] STATUSES = {"Applied", "Shortlisted", "Interview Scheduled", "Hired", "Rejected"};
    public static final List<User> users = new ArrayList<>();
    public static final List<Job> jobs = new ArrayList<>();
    public static final List<App> apps = new ArrayList<>();
    public static User me;
    static final File FILE = new File("data/recruitment.dat");

    /** Load saved data (or seed demo data on first run) and save automatically on exit. */
    public static void init() {
        if (!load()) seed();
        Runtime.getRuntime().addShutdownHook(new Thread(Service::save));
    }
    static void seed() {
        users.add(new User("Admin", "admin@rs.com", "admin123", "Administrator", 2, 10));
        users.add(new User("Sarah", "sarah@rs.com", "hr12345", "HR Manager", 2, 8));
        User sharon = new User("Sharon Isaac", "sharon@gmail.com", "sharon123", "Candidate", 1, 1);
        sharon.phone = "+91 1234567890"; sharon.location = "Chennai";
        users.add(sharon);
        jobs.add(new Job("Software Developer", "TCS", "Chennai", "IT Services", 1, 2, "Rs. 6-12 LPA"));
        jobs.add(new Job("Data Analyst", "Infosys", "Bengaluru", "Data Analytics", 1, 0, "Rs. 4-8 LPA"));
        jobs.add(new Job("Frontend Developer", "Wipro", "Chennai", "Web Development", 1, 1, "Rs. 5-10 LPA"));
        jobs.add(new Job("UI/UX Designer", "Accenture", "Hyderabad", "Design", 1, 1, "Rs. 6-10 LPA"));
        jobs.add(new Job("Solutions Architect", "Cognizant", "Pune", "IT Services", 2, 5, "Rs. 20-30 LPA"));
    }
    @SuppressWarnings("unchecked")
    static boolean load() {
        if (!FILE.exists()) return false;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE))) {
            users.addAll((List<User>) in.readObject());
            jobs.addAll((List<Job>) in.readObject());
            apps.addAll((List<App>) in.readObject());
            return true;
        } catch (Exception e) { users.clear(); jobs.clear(); apps.clear(); return false; }
    }
    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
                out.writeObject(new ArrayList<>(users)); out.writeObject(new ArrayList<>(jobs)); out.writeObject(new ArrayList<>(apps));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ---------- Business rules / validation ----------
    public static String hash(String s) {
        try {
            StringBuilder sb = new StringBuilder();
            for (byte b : java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes())) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static User find(String email) {
        return users.stream().filter(u -> u.email.equalsIgnoreCase(email)).findFirst().orElse(null);
    }
    public static String register(String n, String e, String p, String role, int lvl, int exp, String phone) {
        if (n.isBlank() || e.isBlank() || p.isEmpty()) return "Name, email and password are required.";
        if (!e.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) return "Enter a valid email address.";
        if (p.length() < 6) return "Password must be at least 6 characters.";
        if (find(e) != null) return "An account with this email already exists. Please log in.";
        User u = new User(n, e, p, role, lvl, exp);
        u.phone = phone;
        users.add(u);
        return null;
    }
    public static String login(String e, String p, String role) {
        if (e.isBlank() || p.isEmpty()) return "Enter your email and password.";
        User u = find(e);
        if (u == null) return "No account found for this email. Please register first.";
        if (!u.hash.equals(hash(p))) return "Incorrect password.";
        if (!u.role.equals(role)) return "This account is registered as " + u.role + ", not " + role + ".";
        me = u;
        return null;
    }
    /** Returns null if eligible, otherwise the reason(s). */
    public static String eligibility(User c, Job j) {
        List<String> why = new ArrayList<>();
        if (c.level < j.minLevel) why.add("requires " + LEVELS[j.minLevel] + " or higher (yours: " + LEVELS[c.level] + ")");
        if (c.exp < j.minExp) why.add("requires " + j.minExp + "+ years of experience (yours: " + c.exp + ")");
        return why.isEmpty() ? null : "You are not eligible: " + String.join("; ", why) + ".";
    }
    public static String apply(User c, Job j) {
        if (apps.stream().anyMatch(a -> a.cand == c && a.job == j)) return "You have already applied for this job.";
        String err = eligibility(c, j);
        if (err != null) return err;
        apps.add(new App(c, j));
        return null;
    }
    public static long cnt(User c, String s) {
        return apps.stream().filter(a -> (c == null || a.cand == c) && (s == null || a.status.equals(s))).count();
    }

}
