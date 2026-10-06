package recruitment.backend;

public class User implements java.io.Serializable {
    public String name, email, hash, role, phone = "", location = "";
    public int level, exp;
    public User(String n, String e, String p, String r, int l, int x) {
        name = n; email = e; hash = Service.hash(p); role = r; level = l; exp = x;
    }
}
