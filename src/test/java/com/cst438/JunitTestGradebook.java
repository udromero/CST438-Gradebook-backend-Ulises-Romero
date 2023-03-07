package com.cst438;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import com.cst438.controllers.GradeBookController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradebookDTO;
import com.cst438.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;

/* 
 * Example of using Junit with Mockito for mock objects
 *  the database repositories are mocked with test data.
 *  
 * Mockmvc is used to test a simulated REST call to the RestController
 * 
 * the http response and repository is verified.
 * 
 *   Note: This tests uses Junit 5.
 *  ContextConfiguration identifies the controller class to be tested
 *  addFilters=false turns off security.  (I could not get security to work in test environment.)
 *  WebMvcTest is needed for test environment to create Repository classes.
 */
@ContextConfiguration(classes = { GradeBookController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestGradebook {

	static final String URL = "http://localhost:8081";
	public static final int TEST_COURSE_ID = 40442;
	public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
	public static final String TEST_STUDENT_NAME = "test";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int TEST_YEAR = 2021;
	public static final String TEST_SEMESTER = "Fall";

	@MockBean
	AssignmentRepository assignmentRepository;

	@MockBean
	AssignmentGradeRepository assignmentGradeRepository;

	@MockBean
	CourseRepository courseRepository; // must have this to keep Spring test happy

	@MockBean
	RegistrationService registrationService; // must have this to keep Spring test happy

	@Autowired
	private MockMvc mvc;

	@Test
	public void gradeAssignment() throws Exception {

		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set dueDate to 1 week before now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);

		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment);
		ag.setId(1);
		ag.setScore("");
		ag.setStudentEnrollment(enrollment);

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(null);
		given(assignmentGradeRepository.save(any())).willReturn(ag);

		// end of mock data

		// then do an http get request for assignment 1
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that a save was called on repository
		verify(assignmentGradeRepository, times(1)).save(any()); // ???

		// verify that returned data has non zero primary key
		GradebookDTO result = fromJsonString(response.getContentAsString(), GradebookDTO.class);
		// assignment id is 1
		assertEquals(1, result.assignmentId);
		// there is one student list
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("", result.grades.get(0).grade);

		// change grade to score = 80
		result.grades.get(0).grade = "80";

		given(assignmentGradeRepository.findById(1)).willReturn(Optional.of(ag));

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());

		AssignmentGrade updatedag = new AssignmentGrade();
		updatedag.setId(1);
		updatedag.setScore("80");

		// verify that repository saveAll method was called
		verify(assignmentGradeRepository, times(1)).save(updatedag);
	}

	@Test
	public void updateAssignmentGrade() throws Exception {

		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());

		Enrollment enrollment = new Enrollment();
		enrollment.setCourse(course);
		course.getEnrollments().add(enrollment);
		enrollment.setId(TEST_COURSE_ID);
		enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
		enrollment.setStudentName(TEST_STUDENT_NAME);

		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set dueDate to 1 week before now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
		assignment.setId(1);
		assignment.setName("Assignment 1");
		assignment.setNeedsGrading(1);

		AssignmentGrade ag = new AssignmentGrade();
		ag.setAssignment(assignment);
		ag.setId(1);
		ag.setScore("80");
		ag.setStudentEnrollment(enrollment);

		// given -- stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(ag);
		given(assignmentGradeRepository.findById(1)).willReturn(Optional.of(ag));

		// end of mock data

		// then do an http get request for assignment 1
		response = mvc.perform(MockMvcRequestBuilders.get("/gradebook/1").accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify return data with entry for one student without no score
		assertEquals(200, response.getStatus());

		// verify that a save was NOT called on repository because student already has a
		// grade
		verify(assignmentGradeRepository, times(0)).save(any());

		// verify that returned data has non zero primary key
		GradebookDTO result = fromJsonString(response.getContentAsString(), GradebookDTO.class);
		// assignment id is 1
		assertEquals(1, result.assignmentId);
		// there is one student list
		assertEquals(1, result.grades.size());
		assertEquals(TEST_STUDENT_NAME, result.grades.get(0).name);
		assertEquals("80", result.grades.get(0).grade);

		// change grade to score = 88
		result.grades.get(0).grade = "88";

		// send updates to server
		response = mvc
				.perform(MockMvcRequestBuilders.put("/gradebook/1").accept(MediaType.APPLICATION_JSON)
						.content(asJsonString(result)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// verify that return status = OK (value 200)
		assertEquals(200, response.getStatus());

		// verify that repository save method was called
		// AssignmentGrade must override equals method for this test for work !!!
		AssignmentGrade updatedag = new AssignmentGrade();
		updatedag.setId(1);
		updatedag.setScore("88");
		verify(assignmentGradeRepository, times(1)).save(updatedag);
	}
	
	@Test 
	 public void createNewAssignment() throws Exception{
		
		MockHttpServletResponse response;
		
		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());
				
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set due date on week ago
		assignment.setId(0);
		final java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
		assignment.setDueDate(sqlDate);
		assignment.setName("Assignment1");
		assignment.setNeedsGrading(1);
		
		// for course repository given a course id, return the test data
		given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
		
		// A http put request to create a new assignment
		response = mvc.perform(MockMvcRequestBuilders.post("/assignment?email=" + TEST_INSTRUCTOR_EMAIL + "&id="+ TEST_COURSE_ID + "&name=Assignment1&due_date=2023-04-20")
				.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// verify no errors occurred inserting the new assignment
		assertEquals(200, response.getStatus());
		
		// verify the save method was called at least one for any one assignment
		verify(assignmentRepository, times(1)).save(any(Assignment.class)); 
		
	}
	
	@Test
	public void updateAssignmentName() throws Exception{
		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());
		
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		assignment.setName("Assignment 1");
		assignment.setId(1);
		
		// given - stubs for database repositories that return test data
		given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
		given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(null);
		
		// verify that repository save method wasn't called
		verify(assignmentRepository, times(0)).save(assignment);

		// then do an http put request for assignment 1 to change its name
		response = mvc.perform(MockMvcRequestBuilders.put("/assignment/1?email=" + TEST_INSTRUCTOR_EMAIL + "&new_name=NewAssignmentName")
				.accept(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();
		
		// verify return status OK = 200
		assertEquals(200, response.getStatus());
		
		// verify that repository save method was called
		verify(assignmentRepository, times(1)).save(assignment);
	}
	
	@Test 
	public void deleteAssignment() throws Exception{
		MockHttpServletResponse response;

		// mock database data

		Course course = new Course();
		course.setCourse_id(TEST_COURSE_ID);
		course.setSemester(TEST_SEMESTER);
		course.setYear(TEST_YEAR);
		course.setInstructor(TEST_INSTRUCTOR_EMAIL);
		course.setEnrollments(new java.util.ArrayList<Enrollment>());
		course.setAssignments(new java.util.ArrayList<Assignment>());
		
		Assignment assignment = new Assignment();
		assignment.setCourse(course);
		course.getAssignments().add(assignment);
		// set dueDate to now.
		assignment.setDueDate(new java.sql.Date(System.currentTimeMillis()));
		assignment.setId(1);
		assignment.setName("Assignment2Delete");
		assignment.setNeedsGrading(0);
		
		// check course assignments list is size of 1
		assertEquals(1, course.getAssignments().size());
		
		// given -- stubs for database repositories that return test data
	    given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
	    given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(null);
	    
	    // delete assignment one using a http delete request
	    response = mvc.perform(MockMvcRequestBuilders.delete("/assignment/1?email=" + TEST_INSTRUCTOR_EMAIL).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
	    
	    // verify return status OK = 200
	 	assertEquals(200, response.getStatus());
	 	
	 	// verify assignment was deleted
	 	verify(assignmentRepository, times(1)).delete(assignment);
	}

	private static String asJsonString(final Object obj) {
		try {

			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T fromJsonString(String str, Class<T> valueType) {
		try {
			return new ObjectMapper().readValue(str, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
