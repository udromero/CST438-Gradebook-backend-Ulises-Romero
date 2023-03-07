//package com.cst438.controllers;
//
//import java.sql.Date;
//
//import javax.transaction.Transactional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.cst438.domain.Assignment;
//import com.cst438.domain.AssignmentGradeRepository;
//import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
//import com.cst438.domain.AssignmentRepository;
//import com.cst438.domain.Course;
//import com.cst438.domain.CourseRepository;
//
//@RestController
//@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
//public class InstructorController {
//	
//	@Autowired
//	AssignmentRepository assignmentRepository;
//	
//	@Autowired
//	AssignmentGradeRepository assignmentGradeRepository;
//	
//	@Autowired
//	CourseRepository courseRepository;
//	
//	// Add an assignment for a course, include name and due date
//	@PostMapping("/instructor/{instructor_email}/course/{course_id}/add_assignment/{name}/{due_date}")
//	@Transactional
//    public void createNewAssignment(@PathVariable String instructor_email,
//                                           @PathVariable int course_id,
//                                           @PathVariable String name,
//                                           @PathVariable Date due_date) {
//		
//		//Get course
//        Course course = courseRepository.findById(course_id).orElse(null);
//        
//        if (course == null) {
//            throw new IllegalArgumentException("Course not found");
//        }
//        
//        // Set new assignments values;
//        Assignment assignment = new Assignment();
//        assignment.setCourse(course);
//        assignment.setName(name);
//        assignment.setDueDate(due_date);
//        assignment.setNeedsGrading(0);
//     
//        assignmentRepository.save(assignment);
//    }
//	
//	// Update an existing assignment for a course with a new name
//	@PutMapping("/instructor/{instructor_email}/course/{course_id}/update_assignment_name/{assignment_id}/{new_name}")
//	@Transactional
//	public void updateAssignmentName(@PathVariable String instructor_email,
//	                                  @PathVariable int course_id,
//	                                  @PathVariable int assignment_id,
//	                                  @PathVariable String new_name) {
//	    // Get assignment
//	    Assignment assignment = assignmentRepository.findById(assignment_id).orElse(null);
//	    
//	    if (assignment == null) {
//	        throw new IllegalArgumentException("Assignment not found");
//	    }
//	    
//	    // Check if the instructor is authorized to modify the assignment
//	    if (!assignment.getCourse().getInstructor().equals(instructor_email)) {
//	        throw new IllegalArgumentException("Instructor not authorized to modify this assignment");
//	    }
//	    
//	    // Update assignment name
//	    assignment.setName(new_name);
//	    assignmentRepository.save(assignment);
//	}
//	
//	// Delete an existing assignment for a course
//	@PutMapping("/instructor/{instructor_email}/course/{course_id}/delete_assignment/{assignment_id}")
//	@Transactional
//	public void deleteAssignment(@PathVariable String instructor_email,
//	                             @PathVariable int course_id,
//	                             @PathVariable int assignment_id) {
//	    // Get assignment
//	    Assignment assignment = assignmentRepository.findById(assignment_id).orElse(null);
//	    
//	    if (assignment == null) {
//	        throw new IllegalArgumentException("Assignment not found");
//	    }
//	    
//	    // Check if the instructor is authorized to delete the assignment
//	    if (!assignment.getCourse().getInstructor().equals(instructor_email)) {
//	        throw new IllegalArgumentException("Instructor not authorized to delete this assignment");
//	    }
//	 
//	    // Check if the assignment has any grades
//	    if (assignment.getNeedsGrading() == 0) {
//	        throw new IllegalArgumentException("Cannot delete assignment with existing grades");
//	    }
//	    
//	    // Delete assignment
//	    assignmentRepository.delete(assignment);
//	}
//	
//	
//}
