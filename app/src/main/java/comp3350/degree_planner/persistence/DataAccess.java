package comp3350.degree_planner.persistence;

import java.util.ArrayList;

import comp3350.degree_planner.objects.Course;
import comp3350.degree_planner.objects.CourseOffering;
import comp3350.degree_planner.objects.CourseResult;
import comp3350.degree_planner.objects.Degree;
import comp3350.degree_planner.objects.Department;
import comp3350.degree_planner.objects.TermType;

/**
 * Created by Tiffany Jiang on 2017-06-06.
 * <p>
 * Interface for data access
 */

public interface DataAccess {
    void open();

    void close();

    ArrayList<Course> getCoursesNotTaken(int studentNumber);

    Course getCourse(CourseResult courseResult, ArrayList<Course> allCourses);

    ArrayList<Course> getAllCourses();

    ArrayList<Course> getCoursesCanTake(int studentNumber);

    boolean hasPrerequisites(int studentNumber, String courseName);

    ArrayList<Course> getAllPrerequisites(Course course);

    Course findCourse(int courseId);

    Course findCourse(String courseName);

    ArrayList<Degree> getAllDegrees();

    Degree getDegreeByName(String degreeName);

    Degree getDegreeById(int degreeId);

    ArrayList<CourseResult> getCourseResultsByStudentId(int studentId);

    int getFailingGradeId();

    Course getCourseById(int courseId);

    ArrayList<CourseOffering> getCourseOfferingsByTerm(TermType type);

    Department getDepartmentById(int departmentId);

    ArrayList<Course> getCoursesTaken(int studentId);

    ArrayList<Course> getDegreeCoursesTaken(int studentId, int degreeId);

    ArrayList<Course> getDegreeCourses(int degreeId);

    ArrayList<Course> getEligibleRequiredCourse(int studentNum, int degreeId);
}