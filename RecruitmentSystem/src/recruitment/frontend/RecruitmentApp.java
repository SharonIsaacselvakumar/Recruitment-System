package recruitment.frontend;

import recruitment.backend.*;
import static recruitment.backend.Service.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/** Frontend: Swing screens (login, candidate / HR / admin dashboards). */
public class RecruitmentApp {
    static final Color HEADER = new Color(0xF4B6EC), SIDE = new Color(0xF9C6FF), SEL = new Color(0xE680F5),
            ACCENT = new Color(0xE8507A), CARD = new Color(0xFFB6B6), BG = new Color(0xFDF3FA);
    static JFrame frame;
    static String query = "", current = "";
    static Consumer<String> open;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        Service.init();
        SwingUtilities.invokeLater(RecruitmentApp::showLogin);
    }

    // ---------- UI helpers ----------
    static void msg(String s) { JOptionPane.showMessageDialog(frame, s); }
    static <T extends JComponent> T at(T c, int x, int y, int w, int h) { c.setBounds(x, y, w, h); return c; }
    static JButton btn(String t, Runnable r) {
        JButton b = new JButton(t);
        b.setBackground(ACCENT); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        b.addActionListener(e -> r.run());
        return b;
    }
    static JPanel flow() { JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); p.setOpaque(false); return p; }
    static JPanel form(Object... kv) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8)); p.setOpaque(false);
        for (int i = 0; i < kv.length; i += 2) { p.add(new JLabel((String) kv[i])); p.add((JComponent) kv[i + 1]); }
        return p;
    }
    static JPanel page(String title, String sub, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG); p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.add(new JLabel("<html><b style='font-size:16px'>" + title + "</b><br><i>" + sub + "</i></html>"), BorderLayout.NORTH);
        p.add(body);
        return p;
    }
    static JPanel stats(Object... kv) {
        JPanel p = new JPanel(new GridLayout(1, 0, 12, 0)); p.setOpaque(false);
        for (int i = 0; i < kv.length; i += 2) {
            JLabel l = new JLabel("<html><center><span style='font-size:22px;color:#E8507A'>" + kv[i + 1] + "</span><br>" + kv[i] + "</center></html>", SwingConstants.CENTER);
            l.setOpaque(true); l.setBackground(Color.WHITE);
            l.setBorder(new CompoundBorder(new LineBorder(new Color(0xF4C6DC)), new EmptyBorder(14, 10, 14, 10)));
            p.add(l);
        }
        return p;
    }
    static JTable table(String[] cols, List<Object[]> rows) {
        JTable t = new JTable(new DefaultTableModel(rows.toArray(new Object[0][]), cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        t.setRowHeight(26); t.getTableHeader().setBackground(HEADER); t.setSelectionBackground(SEL);
        return t;
    }
    static JPanel wrap(JTable t, JComponent south) {
        JPanel p = new JPanel(new BorderLayout(0, 8)); p.setOpaque(false);
        p.add(new JScrollPane(t)); p.add(south, BorderLayout.SOUTH);
        return p;
    }
    static JPanel stack(JComponent top, JComponent center) {
        JPanel p = new JPanel(new BorderLayout(0, 12)); p.setOpaque(false);
        p.add(top, BorderLayout.NORTH); p.add(center);
        return p;
    }

    // ---------- Dialogs ----------
    static boolean userDialog(boolean admin) {
        JTextField n = new JTextField(), e = new JTextField(), ph = new JTextField();
        JPasswordField p = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(ROLES), q = new JComboBox<>(LEVELS);
        JSpinner x = new JSpinner(new SpinnerNumberModel(0, 0, 40, 1));
        JPanel f = admin ? form("Role", role, "Full Name", n, "Email", e, "Password", p, "Phone", ph, "Qualification", q, "Experience (yrs)", x)
                : form("Full Name", n, "Email", e, "Password", p, "Phone", ph, "Qualification", q, "Experience (yrs)", x);
        while (JOptionPane.showConfirmDialog(frame, f, admin ? "Add User" : "Register as Candidate",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String err = register(n.getText().trim(), e.getText().trim(), new String(p.getPassword()),
                    admin ? (String) role.getSelectedItem() : "Candidate", q.getSelectedIndex(), (Integer) x.getValue(), ph.getText().trim());
            if (err == null) { msg("Account created successfully."); return true; }
            msg(err);
        }
        return false;
    }
    static boolean jobDialog() {
        JTextField t = new JTextField(), c = new JTextField(), l = new JTextField(), d = new JTextField(), s = new JTextField();
        JComboBox<String> q = new JComboBox<>(LEVELS);
        JSpinner x = new JSpinner(new SpinnerNumberModel(0, 0, 40, 1));
        JPanel f = form("Title", t, "Company", c, "Location", l, "Department", d, "Min Qualification", q, "Min Experience (yrs)", x, "Salary", s);
        while (JOptionPane.showConfirmDialog(frame, f, "Add Job", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            if (t.getText().isBlank() || c.getText().isBlank()) { msg("Title and company are required."); continue; }
            if (jobs.stream().anyMatch(j -> j.title.equalsIgnoreCase(t.getText().trim()) && j.company.equalsIgnoreCase(c.getText().trim()))) {
                msg("This job is already posted."); continue;
            }
            jobs.add(new Job(t.getText().trim(), c.getText().trim(), l.getText().trim(), d.getText().trim(),
                    q.getSelectedIndex(), (Integer) x.getValue(), s.getText().trim()));
            return true;
        }
        return false;
    }

    // ---------- Login screen ----------
    static void showLogin() {
        JFrame f = new JFrame("Recruitment System - Login");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setSize(900, 540); f.setResizable(false); f.setLocationRelativeTo(null);
        JPanel bg = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setPaint(new GradientPaint(0, 0, new Color(0xB5DEFA), getWidth(), getHeight(), new Color(0xF8D7E8)));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JPanel card = new JPanel(null); card.setBackground(CARD); at(card, 70, 90, 340, 320); bg.add(card);
        JTextField em = new JTextField(); JPasswordField pw = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(ROLES);
        JLabel t = at(new JLabel("LOGIN", SwingConstants.CENTER), 0, 20, 340, 30);
        t.setFont(new Font("Serif", Font.BOLD, 22)); card.add(t);
        card.add(at(new JLabel("EMAIL:"), 20, 75, 100, 25)); card.add(at(em, 130, 75, 190, 26));
        card.add(at(new JLabel("PASSWORD:"), 20, 115, 100, 25)); card.add(at(pw, 130, 115, 190, 26));
        card.add(at(new JLabel("DESIGNATION:"), 20, 155, 100, 25)); card.add(at(role, 130, 155, 190, 26));
        JButton in = btn("Login", () -> {
            String err = login(em.getText().trim(), new String(pw.getPassword()), (String) role.getSelectedItem());
            if (err != null) { JOptionPane.showMessageDialog(f, err, "Login failed", JOptionPane.ERROR_MESSAGE); return; }
            f.dispose(); showMain();
        });
        card.add(at(in, 40, 230, 120, 34));
        card.add(at(btn("Register", () -> userDialog(false)), 180, 230, 120, 34));
        card.add(at(new JLabel("New candidates: click Register", SwingConstants.CENTER), 0, 280, 340, 20));
        JLabel title = at(new JLabel("<html><center>RECRUITMENT<br>SYSTEM</center></html>", SwingConstants.CENTER), 460, 160, 360, 120);
        title.setFont(new Font("Serif", Font.BOLD, 34)); bg.add(title);
        f.setContentPane(bg); f.getRootPane().setDefaultButton(in); f.setVisible(true);
    }

    // ---------- Main shell (header + sidebar + content) ----------
    static void showMain() {
        frame = new JFrame("Recruitment System - " + me.role);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setSize(1150, 720); frame.setLocationRelativeTo(null);
        Map<String, Supplier<JComponent>> nav = navFor();
        JPanel content = new JPanel(new BorderLayout());
        JPanel side = new JPanel(); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDE); side.setBorder(new EmptyBorder(20, 12, 12, 12));
        Map<String, JButton> buttons = new LinkedHashMap<>();
        open = k -> {
            current = k;
            buttons.forEach((n, b) -> b.setBackground(n.equals(k) ? SEL : SIDE));
            content.removeAll(); content.add(nav.get(k).get()); content.revalidate(); content.repaint();
        };
        for (String k : nav.keySet()) {
            JButton b = new JButton(k);
            b.setFont(new Font("Serif", Font.PLAIN, 18)); b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setMaximumSize(new Dimension(190, 40)); b.setBorderPainted(false); b.setFocusPainted(false); b.setBackground(SIDE);
            b.addActionListener(e -> {
                if (k.equals("Log Out")) { frame.dispose(); me = null; showLogin(); }
                else { query = ""; open.accept(k); }
            });
            buttons.put(k, b); side.add(b); side.add(Box.createVerticalStrut(6));
        }
        JPanel head = new JPanel(new BorderLayout(20, 0)); head.setBackground(HEADER); head.setBorder(new EmptyBorder(8, 16, 8, 16));
        JLabel logo = new JLabel("<html>Recruitment<br>System</html>"); logo.setFont(new Font("Serif", Font.BOLD, 18));
        JTextField search = new JTextField(); search.setToolTipText("Candidates: type a job, company or city and press Enter");
        search.addActionListener(e -> { if (me.role.equals("Candidate")) { query = search.getText().trim(); open.accept("Search Jobs"); } });
        head.add(logo, BorderLayout.WEST); head.add(search); head.add(new JLabel(me.name + " (" + me.role + ")"), BorderLayout.EAST);
        frame.add(head, BorderLayout.NORTH); frame.add(side, BorderLayout.WEST); frame.add(content);
        open.accept("Dashboard"); frame.setVisible(true);
    }

    static Map<String, Supplier<JComponent>> navFor() {
        Map<String, Supplier<JComponent>> m = new LinkedHashMap<>();
        switch (me.role) {
            case "Candidate" -> {
                m.put("Dashboard", () -> candDash());
                m.put("Search Jobs", () -> page("Search Jobs", "Find a job and apply", jobsView(query)));
                m.put("Applications", () -> appsView(me, null, false));
                m.put("Interviews", () -> appsView(me, "Interview Scheduled", false));
            }
            case "HR Manager" -> {
                m.put("Dashboard", () -> hrDash());
                m.put("Job Vacancies", () -> manageJobs());
                m.put("Applications", () -> appsView(null, null, true));
                m.put("Shortlist", () -> appsView(null, "Shortlisted", true));
                m.put("Interviews", () -> appsView(null, "Interview Scheduled", true));
                m.put("Reports", () -> reports());
            }
            default -> {
                m.put("Dashboard", () -> adminDash());
                m.put("Manage Users", () -> manageUsers());
                m.put("Job Categories", () -> categories());
                m.put("Reports", () -> reports());
            }
        }
        m.put("Profile", () -> profile());
        m.put("Log Out", () -> null);
        return m;
    }

    // ---------- Screens ----------
    static JComponent candDash() {
        JPanel s = stats("Applied Jobs", cnt(me, null), "Interviews Scheduled", cnt(me, "Interview Scheduled"),
                "Offers Received", cnt(me, "Hired"), "Pending Applications", cnt(me, "Applied"));
        return page("Welcome, " + me.name + "!", "Explore opportunities and take the next step", stack(s, jobsView("")));
    }
    static JComponent hrDash() {
        JPanel s = stats("Total Jobs", jobs.size(), "Total Applications", cnt(null, null),
                "Shortlisted", cnt(null, "Shortlisted"), "Interviews Scheduled", cnt(null, "Interview Scheduled"));
        return page("Welcome, " + me.name + "!", "Here's what's happening with recruitment today", stack(s, appsView(null, null, false)));
    }
    static JComponent adminDash() {
        long cands = users.stream().filter(u -> u.role.equals("Candidate")).count();
        long hrs = users.stream().filter(u -> u.role.equals("HR Manager")).count();
        JPanel s = stats("Total Users", users.size(), "Total Candidates", cands, "HR Managers", hrs, "Total Jobs", jobs.size());
        return page("Welcome, Admin!", "Overview of the recruitment system", stack(s, manageUsers()));
    }

    static JComponent jobsView(String q) {
        List<Job> l = jobs.stream().filter(j -> (j.title + j.company + j.location + j.dept).toLowerCase().contains(q.toLowerCase())).toList();
        JTable t = table(new String[]{"Title", "Company", "Location", "Min Qualification", "Min Exp", "Salary", "Eligible?"},
                l.stream().map(j -> new Object[]{j.title, j.company, j.location, LEVELS[j.minLevel], j.minExp + " yrs", j.salary,
                        eligibility(me, j) == null ? "Yes" : "No"}).toList());
        JPanel s = flow();
        s.add(btn("Apply", () -> {
            int r = t.getSelectedRow();
            if (r < 0) { msg("Select a job first."); return; }
            String err = apply(me, l.get(r));
            msg(err == null ? "Application submitted successfully!" : err);
            if (err == null) open.accept(current);
        }));
        return wrap(t, s);
    }

    static JComponent appsView(User c, String st, boolean manage) {
        List<App> l = apps.stream().filter(a -> (c == null || a.cand == c) && (st == null || a.status.equals(st))).toList();
        JTable t = table(new String[]{"Candidate", "Qualification", "Exp", "Job", "Company", "Status"},
                l.stream().map(a -> new Object[]{a.cand.name, LEVELS[a.cand.level], a.cand.exp + " yrs", a.job.title, a.job.company, a.status}).toList());
        JPanel s = flow();
        if (manage) {
            String[][] actions = {{"Shortlist", "Shortlisted"}, {"Schedule Interview", "Interview Scheduled"}, {"Hire", "Hired"}, {"Reject", "Rejected"}};
            for (String[] a : actions) s.add(btn(a[0], () -> {
                int r = t.getSelectedRow();
                if (r < 0) { msg("Select an application first."); return; }
                l.get(r).status = a[1]; open.accept(current);
            }));
        }
        return page(st == null ? "Applications" : st, c == null ? "Review and update candidate applications" : "Your applications", wrap(t, s));
    }

    static JComponent manageJobs() {
        JTable t = table(new String[]{"Title", "Company", "Location", "Department", "Min Qualification", "Min Exp", "Salary"},
                jobs.stream().map(j -> new Object[]{j.title, j.company, j.location, j.dept, LEVELS[j.minLevel], j.minExp + " yrs", j.salary}).toList());
        JPanel s = flow();
        s.add(btn("Add Job", () -> { if (jobDialog()) open.accept(current); }));
        s.add(btn("Remove Job", () -> {
            int r = t.getSelectedRow();
            if (r < 0) { msg("Select a job first."); return; }
            Job j = jobs.get(r); jobs.remove(j); apps.removeIf(a -> a.job == j); open.accept(current);
        }));
        return page("Job Vacancies", "Post and manage jobs", wrap(t, s));
    }

    static JComponent manageUsers() {
        JTable t = table(new String[]{"Name", "Email", "Role", "Qualification", "Exp"},
                users.stream().map(u -> new Object[]{u.name, u.email, u.role, LEVELS[u.level], u.exp + " yrs"}).toList());
        JPanel s = flow();
        s.add(btn("Add User", () -> { if (userDialog(true)) open.accept(current); }));
        s.add(btn("Delete User", () -> {
            int r = t.getSelectedRow();
            if (r < 0) { msg("Select a user first."); return; }
            User u = users.get(r);
            if (u == me) { msg("You cannot delete your own account."); return; }
            users.remove(u); apps.removeIf(a -> a.cand == u); open.accept(current);
        }));
        return page("Manage Users", "Add or remove users", wrap(t, s));
    }

    static JComponent categories() {
        Map<String, Long> g = jobs.stream().collect(Collectors.groupingBy(j -> j.dept, TreeMap::new, Collectors.counting()));
        JTable t = table(new String[]{"Job Category", "Open Jobs"}, g.entrySet().stream().map(e -> new Object[]{e.getKey(), e.getValue()}).toList());
        return page("Job Categories", "Categories come from posted jobs", wrap(t, flow()));
    }

    static JComponent reports() {
        Object[] kv = new Object[STATUSES.length * 2];
        for (int i = 0; i < STATUSES.length; i++) { kv[2 * i] = STATUSES[i]; kv[2 * i + 1] = cnt(null, STATUSES[i]); }
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(stats(kv), BorderLayout.NORTH);
        return page("Reports", "Application status summary (" + cnt(null, null) + " total)", top);
    }

    static JComponent profile() {
        JTextField n = new JTextField(me.name), e = new JTextField(me.email), ph = new JTextField(me.phone), lo = new JTextField(me.location);
        e.setEditable(false);
        JComboBox<String> q = new JComboBox<>(LEVELS); q.setSelectedIndex(me.level);
        JSpinner x = new JSpinner(new SpinnerNumberModel(me.exp, 0, 40, 1));
        JPanel f = me.role.equals("Candidate")
                ? form("Full Name", n, "Email", e, "Phone", ph, "Location", lo, "Qualification", q, "Experience (yrs)", x)
                : form("Full Name", n, "Email", e, "Phone", ph, "Location", lo);
        JPanel s = flow();
        s.add(btn("Save Profile", () -> {
            if (n.getText().isBlank()) { msg("Name cannot be empty."); return; }
            me.name = n.getText().trim(); me.phone = ph.getText().trim(); me.location = lo.getText().trim();
            me.level = q.getSelectedIndex(); me.exp = (Integer) x.getValue();
            msg("Profile updated.");
        }));
        JPanel box = new JPanel(new BorderLayout(0, 10)); box.setOpaque(false); box.setBorder(new EmptyBorder(0, 0, 0, 300));
        box.add(f); box.add(s, BorderLayout.SOUTH);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(box, BorderLayout.NORTH);
        return page("My Profile", "Manage your personal information", top);
    }
}
