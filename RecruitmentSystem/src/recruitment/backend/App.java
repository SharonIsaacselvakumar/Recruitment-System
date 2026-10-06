package recruitment.backend;

/** A job application (candidate + job + status). */
public class App implements java.io.Serializable {
    public User cand; public Job job; public String status = "Applied";
    public App(User c, Job j) { cand = c; job = j; }
}
