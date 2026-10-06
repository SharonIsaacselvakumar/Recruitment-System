package recruitment.backend;

public class Job implements java.io.Serializable {
    public String title, company, location, dept, salary;
    public int minLevel, minExp;
    public Job(String t, String c, String l, String d, int ml, int mx, String s) {
        title = t; company = c; location = l; dept = d; minLevel = ml; minExp = mx; salary = s;
    }
}
