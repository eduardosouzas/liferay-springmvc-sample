package com.aspire.student.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aspire.student.constant.Constants;
import com.aspire.student.model.Student;
import com.aspire.student.service.CourseCategoryService;
import com.aspire.student.service.CourseService;
import com.aspire.student.service.StudentService;
import com.aspire.student.util.JsonServiceUtil;
import com.aspire.student.vo.StudentVO;

/**
 * Controller for manages student
 * 
 * @author aspire
 * 
 */
@Controller("studentController")
@RequestMapping("VIEW")
public class StudentController {

  @Autowired
  private StudentService studentService;
  @Autowired
  private CourseService courseService;
  @Autowired
  private CourseCategoryService courseCategoryService;
  @Autowired
  private MessageSource messageSource;
  private Logger logger = Logger.getLogger(StudentController.class);

  /**
   * Default render method when page is load
   * 
   * @param map
   * @return
   */
  @RenderMapping
  public String defaultRender(Map<String, Object> map) {
    logger.debug("in defaultRender()");
    return "studentList";
  }

  /**
   * Render and set required objects for student form
   * 
   * @param map
   * @return
   */
  @RenderMapping(params = "render=studentForm")
  public String addStudent(@RequestParam(value = "studentId", required = false) Integer studentId,
      Map<String, Object> map) {
    logger.debug("in addStudent()");
    Student student = null;
    if (studentId != null) {
      logger.info("student id is " + studentId);
      student = studentService.getStudent(studentId);
    }
    if (student == null) {
      student = new Student();
    }
    try {
      map.put("student", student);
      map.put("courseCategory", courseCategoryService.getCourseCategories());
      map.put("courses", courseService.getCourseList());
    } catch (Exception e) {
      logger.error("Error while rendering objects", e);
    }
    return "studentForm";
  }

  /**
   * View student data
   * @param studentId
   * @param map
   * @return
   */
  @RenderMapping(params = "render=viewStudent")
  public String viewStudent(@RequestParam(value = "studentId", required = false) Integer studentId,
      Map<String, Object> map) {
    logger.debug("in viewStudent()");
    Student student = null;
    StudentVO studentVO = null;
    if (studentId != null) {
      logger.info("student id is " + studentId);
      student = studentService.getStudent(studentId);
      studentVO = new StudentVO();
      BeanUtils.copyProperties(student, studentVO);
    }
    if (studentVO != null) {
      map.put("student", studentVO);
    }
    return "viewStudent";
  }
  
  /**
   * Create a new student
   * 
   * @param actionRequest
   * @param actionResponse
   * @param model
   * @param student
   * @param result
   * @throws IOException
   * @throws PortletException
   */
  @ActionMapping(params = "action=save")
  public void saveStudent(ActionRequest actionRequest, ActionResponse actionResponse, Model model,
      @ModelAttribute("student") Student student, BindingResult result) throws IOException,
      PortletException {
    logger.debug("In adddStudent()");
    if(student.getCourse().getId()==null){
      logger.info("cource is null");
      student.setCourse(null);
    }
    try {
      student = studentService.addStudent(student);
    } catch (Exception e) {
      logger.error("Error while add new student", e);
    }
    if (student != null) {
      actionRequest.setAttribute(Constants.SUCCESS,
          messageSource.getMessage("student.save.success", null, null));
    } else {
      actionRequest.setAttribute(Constants.ERROR,
          messageSource.getMessage("student.error.success", null, null));
    }
  }

  /**
   * Delete Student
   * @param studentId
   * @param request
   * @param response
   * @param model
   * @throws IOException
   * @throws PortletException
   */
  @ResourceMapping("deleteStudent")
  public void deleteStudent(@RequestParam("studentId") Integer studentId,
      ResourceRequest request, ResourceResponse response, Model model) throws IOException,
      PortletException {
    logger.debug("In deleteStudent()");
    logger.info("Student Id=" + studentId);
    PrintWriter writer = null;
    Map<String, Object> map = new HashMap<String, Object>();
    String actionMessage = null;
    try {
      writer = response.getWriter();
      studentService.removeStudent(studentId);
      actionMessage = messageSource.getMessage("student.delete.success", null, null);
      map.put("status", Constants.SUCCESS);
    } catch (Exception e) {
      actionMessage = messageSource.getMessage("student.delete.error", null, null);
      map.put("status", Constants.ERROR);
      logger.error("Error while delete student", e);
    }
    map.put("actionMessage",actionMessage );
    JsonServiceUtil.writeJson(writer, map);
  }

  /**
   * Retrieve all students and set in view object
   * 
   * @param request
   * @param response
   */
  @ResourceMapping("getAllStudents")
  public void getAllStudents(ResourceRequest request, ResourceResponse response) {
    logger.info("getAllStudents() - Getting all students");
    List<StudentVO> listStudentVO = new ArrayList<StudentVO>();
    StudentVO studentVO = null;
    PrintWriter writer = null;
    try {
      writer = response.getWriter();
      // Fetch students
      List<Student> listStudents = studentService.getStudentList();
      if (listStudents != null && !listStudents.isEmpty()) {
        logger.info("Student Size is " + listStudents.size());
        // Copy list to all student view
        for (Student student : listStudents) {
          studentVO =  new StudentVO();
          BeanUtils.copyProperties(student, studentVO);
          listStudentVO.add(studentVO);
        }
        logger.info("Student View list size " + listStudentVO.size());
      }
    } catch (Exception e) {
      logger.error("Error while getting all students", e);
    }
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("aaData", listStudentVO);
    JsonServiceUtil.writeJson(writer, map);
  }
}
