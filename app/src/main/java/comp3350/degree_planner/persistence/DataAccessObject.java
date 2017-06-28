package comp3350.degree_planner.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import comp3350.degree_planner.objects.Course;
import comp3350.degree_planner.objects.CourseOffering;
import comp3350.degree_planner.objects.CoursePlan;
import comp3350.degree_planner.objects.CourseResult;
import comp3350.degree_planner.objects.Degree;
import comp3350.degree_planner.objects.Department;
import comp3350.degree_planner.objects.ScienceCourse;
import comp3350.degree_planner.objects.TermType;
import comp3350.degree_planner.objects.UserDefinedCourse;

/**
 * Created by Tiffany Jiang on 2017-06-24.
 *
 * Comments about the database:
 * - There are a total of 14 tables (for 16 objects)
 * - See Degree_Planner.script (in assets->db) for required info, basically same as what's in Objects
 * - Has been filled with same "default" info as stub, as instructed
 *
 * - All courses are in 1 table: Course table contains some NULLABLE columns corresponding to fields belonging
 * to only ScienceCourse or UserDefinedCourse, and also contains a IsUserDefined boolean
 *
 * - Most of the id's are auto-generated (starts at x, increments for each insert),
 * except for: reference type tables and some tables where some combo of its attributes make up the primary key
 * (again, generally same as what we currently have in the objects - if there's no id field, no id column)
 *
 * - Basically I've found out how the db runs is that when the person runs the app on the device the first time,
 * the db is set up through whatever's in Degree_Planner.script
 * - Then you can only make changes to db through executing updates/queries via DataAccessObject.java,
 * changes to Degree_Planner.script will not be run again
 * - If you want to run changes to Degree_Planner.script, you have to go into the Settings --> Apps in the emulator
 * and 'Clear Data' for Degree Planner which will clear the database so the next time you run it,
 * it starts by running the script again
 *
 * - There is currently only 1 student (id 1), please use that as the student id for now,
 * at least until we get login capabilities set up
 *
 * - Apparently the script doesn't like it when we specify which columns to fill in INSERT e.g.
 * INSERT INTO COURSE_RESULT (COURSE_ID, STUDENT_ID, GRADE_TYPE_ID) VALUES (1, 1, 1)
 * So we have to manually specify every column in the script
 * Then we have to specify in CREATE TABLE for the autogenerated ID to start with
 * 1 + however many is manually inserted in the script (See CREATE TABLE COURSE_RESULT line for example)
 * You can, however, insert however you like via executeUpdate
 */

public class DataAccessObject implements DataAccess {
    private Statement st1, st2, st3;
    private Connection c1;
    private ResultSet rs2, rs3, rs4, rs5;

    private String dbName;
    private String dbType;

    private String cmdString;
    private int updateCount;
    private String result;
    private static String EOF = "  ";

    private List<Degree> degrees;
    private List<Course> courses;


    public DataAccessObject(String dbName)
    {
        this.dbName = dbName;
    }

    public void open(String dbPath)
    {
        String url;

        try
        {
            dbType = "HSQL";
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
            url = "jdbc:hsqldb:file:" + dbPath;
            c1 = DriverManager.getConnection(url, "SA", "");
            st1 = c1.createStatement();
            st2 = c1.createStatement();
            st3 = c1.createStatement();
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        System.out.println("Opened " + dbType + " database " + dbPath);
    }

    public void close()
    {
        try
        {	// commit all changes to the database
            cmdString = "shutdown compact";
            rs2 = st1.executeQuery(cmdString);
            c1.close();
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        System.out.println("Closed " + dbType + " database " + dbName);
    }

    public List<Course> getCoursesNotTaken(int studentNumber) {
        List<Course> allCourses;
        List<Course> coursesTaken;
        List<Course> coursesNotTaken;
        Course course;

        course = null;
        allCourses = new ArrayList<Course>();
        coursesTaken = new ArrayList<Course>();
        coursesNotTaken = new ArrayList<Course>();

        try
        {
            cmdString = "Select COURSE_ID from COURSE_RESULT where STUDENT_ID = " + studentNumber;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }

        try
        {
            while(rs2.next()){
                course = getCourseById(Integer.parseInt(rs3.getString("COURSE_ID")));
                coursesTaken.add(course);
            }

            allCourses = getAllCourses();

            for(int i = 0; i < coursesTaken.size(); i++){
                for(int j = 0; j < allCourses.size(); j++){ //search and delete course in all Courses list
                    if(allCourses.get(i) == coursesTaken.get(i)){
                        allCourses.remove(i);
                    }
                }
            }

            coursesNotTaken = allCourses;
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }
        return coursesNotTaken;
    }

    public List<Course> getAllCourses() {
        Course course;
        int id;
        String name;
        double creditHours;
        int courseNumber;
        String description;
        int departmentId;
        String fullAbbreviation;
        Boolean isUserDefined;
        courses = new ArrayList<Course>();

        result = null;
        try
        {
            cmdString = "Select * from Course";
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            while (rs2.next())
            {
                id = Integer.parseInt(rs2.getString("ID"));
                name = rs2.getString("NAME");
                creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
                departmentId = Integer.parseInt(rs2.getString("DEPARTMENT_ID"));
                courseNumber = Integer.parseInt(rs2.getString("COURSE_NUMBER"));
                description = rs2.getString("DESCRIPTION");
                fullAbbreviation = rs2.getString("FULL_ABBREVIATION");
                isUserDefined = Boolean.parseBoolean(rs2.getString("IS_USER_DEFINED"));

                if(isUserDefined){
                    course = new UserDefinedCourse(id, name, creditHours, fullAbbreviation);
                }
                else{
                    course = new ScienceCourse(id, name, creditHours, departmentId, courseNumber, description);
                }

               courses.add(course);
            }
            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return courses;
    }

    public List<Course> getCoursesCanTake(int studentNumber) {
        List<Course> coursesNotTaken;
        List<Course> coursesCanTake;

        coursesNotTaken = getCoursesNotTaken(studentNumber);
        coursesCanTake = new ArrayList<Course>();

        for(int i = 0; i < coursesNotTaken.size(); i++){
            if(hasPrerequisites(studentNumber, (coursesNotTaken.get(i)).getName())){
                coursesCanTake.add(coursesNotTaken.get(i));
            }
        }

        return coursesCanTake;
    }

    public boolean hasPrerequisites(int studentNumber, String courseName) {
        boolean hasPreReqs = true;
        List<Course> preReqs_ofCourse = null;
        List<Course> coursesTaken = new ArrayList<Course>();
        Course course;
        int id;
        String name;
        double creditHours;
        int courseNumber;
        String description;
        int departmentId;
        String fullAbbreviation;
        Boolean isUserDefined;

        course = null;
        result = null;

        try
        {
            cmdString = "Select * from Course where NAME = " + courseName;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            id = Integer.parseInt(rs2.getString("ID"));
            name = rs2.getString("NAME");
            creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
            departmentId = Integer.parseInt(rs2.getString("DEPARTMENT_ID"));
            courseNumber = Integer.parseInt(rs2.getString("COURSE_NUMBER"));
            description = rs2.getString("DESCRIPTION");
            fullAbbreviation = rs2.getString("FULL_ABBREVIATION");
            isUserDefined = Boolean.parseBoolean(rs2.getString("IS_USER_DEFINED"));

            if(isUserDefined){
                course = new UserDefinedCourse(id, name, creditHours, fullAbbreviation);
            }
            else{
                course = new ScienceCourse(id, name, creditHours, departmentId, courseNumber, description);
            }

            preReqs_ofCourse = getAllPrerequisites(course);

            try
            {
                cmdString = "Select COURSE_ID from CourseResult where STUDENT_ID = " + studentNumber;
                rs3 = st2.executeQuery(cmdString);
            }
            catch (Exception e)
            {
                processSQLError(e);
            }

            while(rs3.next()){
                course = getCourseById(Integer.parseInt(rs3.getString("COURSE_ID")));
                coursesTaken.add(course);
            }

            for(int i = 0; i < preReqs_ofCourse.size(); i++) {
                if(!coursesTaken.contains(preReqs_ofCourse.get(i))){
                    hasPreReqs = false;
                }
            }

            rs2.close();
            rs3.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return hasPreReqs;
    }

    public List<Course> getAllPrerequisites(Course course) {
        Course currentCourse;
        int prereqId;
        int id;
        String name;
        double creditHours;
        int courseNumber;
        String description;
        int departmentId;
        String fullAbbreviation;
        Boolean isUserDefined;
        courses = new ArrayList<Course>();

        result = null;
        try
        {
            cmdString = "Select PREREQ_COURSE_ID from CoursePrerequisite where COURSE_ID = " + course.getId();
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            while (rs2.next())
            {
                prereqId = Integer.parseInt(rs2.getString("PREREQ_COURSE_ID"));

                try
                {
                    cmdString = "Select * from Course where COURSE_ID = " + prereqId;
                    rs3 = st2.executeQuery(cmdString);
                }
                catch (Exception e)
                {
                    processSQLError(e);
                }

                id = Integer.parseInt(rs3.getString("ID"));
                name = rs3.getString("NAME");
                creditHours = Double.parseDouble(rs3.getString("CREDIT_HOURS"));
                departmentId = Integer.parseInt(rs3.getString("DEPARTMENT_ID"));
                courseNumber = Integer.parseInt(rs3.getString("COURSE_NUMBER"));
                description = rs3.getString("DESCRIPTION");
                fullAbbreviation = rs3.getString("FULL_ABBREVIATION");
                isUserDefined = Boolean.parseBoolean(rs3.getString("IS_USER_DEFINED"));

                if(isUserDefined){
                    currentCourse = new UserDefinedCourse(id, name, creditHours, fullAbbreviation);
                }
                else{
                    currentCourse = new ScienceCourse(id, name, creditHours, departmentId, courseNumber, description);
                }

                courses.add(currentCourse);
            }
            rs2.close();
            rs3.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return courses;
    }

    public Course getCourseByName(String courseName) {
        Course course;
        int id;
        String name;
        double creditHours;
        int courseNumber;
        String description;
        int departmentId;
        String fullAbbreviation;
        Boolean isUserDefined;

        course = null;
        result = null;
        try
        {
            cmdString = "Select * from Course where NAME = " + courseName;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            id = Integer.parseInt(rs2.getString("ID"));
            name = rs2.getString("NAME");
            creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
            departmentId = Integer.parseInt(rs2.getString("DEPARTMENT_ID"));
            courseNumber = Integer.parseInt(rs2.getString("COURSE_NUMBER"));
            description = rs2.getString("DESCRIPTION");
            fullAbbreviation = rs2.getString("FULL_ABBREVIATION");
            isUserDefined = Boolean.parseBoolean(rs2.getString("IS_USER_DEFINED"));

            if(isUserDefined){
                course = new UserDefinedCourse(id, name, creditHours, fullAbbreviation);
            }
            else{
                course = new ScienceCourse(id, name, creditHours, departmentId, courseNumber, description);
            }

            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return course;
    }

    public List<Degree> getAllDegrees() {
        Degree degree;
        int id;
        String name;
        double creditHours, majorCreditHours, gpaRequired;
        degrees = new ArrayList<Degree>();

        result = null;
        try
        {
            cmdString = "Select * from Degree";
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            while (rs2.next())
            {
                id = Integer.parseInt(rs2.getString("ID"));
                name = rs2.getString("NAME");
                creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
                majorCreditHours = Double.parseDouble(rs2.getString("MAJOR_CREDIT_HOURS"));
                gpaRequired = Double.parseDouble(rs2.getString("GPA_REQUIRED"));
                degree = new Degree(id, name, creditHours, majorCreditHours, gpaRequired);
                degrees.add(degree);
            }
            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return degrees;
    }

    public Degree getDegreeByName(String degreeName) {
        Degree degree;
        int id;
        String name;
        double creditHours, majorCreditHours, gpaRequired;

        degree = null;
        result = null;
        try
        {
            cmdString = "Select * from Degree where name = " +degreeName;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
                id = Integer.parseInt(rs2.getString("ID"));
                name = rs2.getString("NAME");
                creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
                majorCreditHours = Double.parseDouble(rs2.getString("MAJOR_CREDIT_HOURS"));
                gpaRequired = Double.parseDouble(rs2.getString("GPA_REQUIRED"));
                degree = new Degree(id, name, creditHours, majorCreditHours, gpaRequired);

            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return degree;
    }

    public Degree getDegreeById(int degreeId) {
        Degree degree;
        int id;
        String name;
        double creditHours, majorCreditHours, gpaRequired;

        degree = null;
        result = null;
        try
        {
            cmdString = "Select * from Degree where ID = " +degreeId;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            id = Integer.parseInt(rs2.getString("ID"));
            name = rs2.getString("NAME");
            creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
            majorCreditHours = Double.parseDouble(rs2.getString("MAJOR_CREDIT_HOURS"));
            gpaRequired = Double.parseDouble(rs2.getString("GPA_REQUIRED"));
            degree = new Degree(id, name, creditHours, majorCreditHours, gpaRequired);

            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return degree;
    }

    public List<CourseResult> getCourseResultsByStudentId(int studentId) {
        return null;
    }

    public List<CourseOffering> getAllCourseOfferings() {
        return null;
    }

    public int getFailingGradeId() {
        return -1;
    }

    public Course getCourseById(int courseId) {
        Course course;
        int id;
        String name;
        double creditHours;
        int courseNumber;
        String description;
        int departmentId;
        String fullAbbreviation;
        Boolean isUserDefined;

        course = null;
        result = null;
        try
        {
            cmdString = "Select * from Course where ID = " + courseId;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            id = Integer.parseInt(rs2.getString("ID"));
            name = rs2.getString("NAME");
            creditHours = Double.parseDouble(rs2.getString("CREDIT_HOURS"));
            departmentId = Integer.parseInt(rs2.getString("DEPARTMENT_ID"));
            courseNumber = Integer.parseInt(rs2.getString("COURSE_NUMBER"));
            description = rs2.getString("DESCRIPTION");
            fullAbbreviation = rs2.getString("FULL_ABBREVIATION");
            isUserDefined = Boolean.parseBoolean(rs2.getString("IS_USER_DEFINED"));

            if(isUserDefined){
                course = new UserDefinedCourse(id, name, creditHours, fullAbbreviation);
            }
            else{
                course = new ScienceCourse(id, name, creditHours, departmentId, courseNumber, description);
            }

            rs2.close();
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return course;
    }

    public List<CourseOffering> getCourseOfferingsByTerm(TermType type) {
        return null;
    }

    public Department getDepartmentById(int departmentId) {
        return null;
    }

    /*
    Created by Matthew Provencher on 2017-06-27

    Returns a list of courses taken by a given student
    */
    public List<Course> getCoursesTaken(int studentId) {
        List<Course> coursesTaken = new ArrayList<Course>();
        Course course = null;

        try
        {
            cmdString = "Select COURSE_ID from Course_Result where STUDENT_ID = " + studentId;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            while(rs2.next()){
                course = getCourseById(Integer.parseInt(rs3.getString("COURSE_ID")));
                coursesTaken.add(course);
            }
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }
        return coursesTaken;
    }

    /*
        Created by Matthew Provencher on 2017-06-27

        Returns a list of degree required courses student has taken
    */
    public List<Course> getDegreeCoursesTaken(int studentId, int degreeId) {
        List<Course> coursesTaken = getCoursesTaken(studentId);
        List<Course> degreeCourses = getDegreeCourses(degreeId);
        List<Course> takenDegreeCourses = new ArrayList<Course>();

        for (Course degreeCourse : degreeCourses) {
            if (coursesTaken.contains(degreeCourse)) {
                takenDegreeCourses.add(degreeCourse);
            }
        }

        return takenDegreeCourses;
    }

    /*
        Created by Matthew Provencher on 2017-06-27

        Returns a list of courses required by a degree
    */
    public List<Course> getDegreeCourses(int degreeId) {
        List<Course> requiredCourses = new ArrayList<Course>();
        Course course = null;
        final int REQUIRED_COURSE = 1;

        try
        {
            cmdString = "Select COURSE_ID from DEGREE_COURSE WHERE DEGREE_ID = " + degreeId + " AND DEGREE_COURSE_TYPE_ID = " + REQUIRED_COURSE;
            rs2 = st1.executeQuery(cmdString);
        }
        catch (Exception e)
        {
            processSQLError(e);
        }
        try
        {
            while(rs2.next()){
                course = getCourseById(Integer.parseInt(rs3.getString("COURSE_ID")));
                requiredCourses.add(course);
            }
        }
        catch (Exception e)
        {
            result = processSQLError(e);
        }

        return requiredCourses;
    }

    /*
    Created by Matthew Provencher on 2017-06-27

    Returns a list of required degree courses that a given student can take
    */
    public List<Course> getEligibleRequiredCourse(int studentNum, int degreeId) {
        List<Course> coursesTaken = getCoursesTaken(studentNum);
        List<Course> degreeCourses = getDegreeCourses(degreeId);
        List<Course> notTakenDegreeCourses = new ArrayList<Course>();
        List<Course> eligibleDegreeCourses = new ArrayList<Course>();

        for (Course degreeCourse : degreeCourses) {
            if (!(coursesTaken.contains(degreeCourse))) {
                notTakenDegreeCourses.add(degreeCourse);
            }
        }

        for (Course course : notTakenDegreeCourses) {
            if (hasPrerequisites(studentNum, course.getName())) {
                eligibleDegreeCourses.add(course);
            }
        }

        return eligibleDegreeCourses;
    }

    public int addToCoursePlan (int courseId, int studentId, int termTypeId, int year) { return -1;
    }

    public boolean moveCourse (int coursePlanId, int newTermTypeId, int newYear) {
        return false;
    }

    public boolean removeFromCoursePlan (int coursePlanId) {
        return false;
    }

    public CoursePlan getCoursePlanById (int coursePlanId) {
        return null;
    }

    public Course getCourse(CourseResult courseResult, List<Course> allCourses){ return null; }

    public String processSQLError(Exception e)
    {
        String result = "*** SQL Error: " + e.getMessage();

        // Remember, this will NOT be seen by the user!
        e.printStackTrace();

        return result;
    }
}
