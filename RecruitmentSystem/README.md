# Recruitment System (Java Swing)

Requires JDK 17+. No external libraries.

## Structure
- `src/recruitment/backend` - `User`, `Job`, `App` models and `Service` (validation, eligibility, storage in `data/recruitment.dat`)
- `src/recruitment/frontend` - `RecruitmentApp` (Swing UI + `main`)

## Run
- VS Code: install "Extension Pack for Java", open this folder, run `RecruitmentApp.java`
- Terminal: `./run.sh` (Mac/Linux) or `run.bat` (Windows), or `mvn package` then `java -jar target/recruitment-system-1.0.jar`

## Demo logins
| Designation | Email | Password |
|---|---|---|
| Candidate | sharon@gmail.com | sharon123 |
| HR Manager | sarah@rs.com | hr12345 |
| Administrator | admin@rs.com | admin123 |

Delete the `data` folder to reset to demo data.
