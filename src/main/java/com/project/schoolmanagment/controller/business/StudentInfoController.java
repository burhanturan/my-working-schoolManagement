package com.project.schoolmanagment.controller.business;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.payload.request.business.StudentInfoRequest;
import com.project.schoolmanagment.payload.request.business.UpdateStudentInfoRequest;
import com.project.schoolmanagment.payload.response.business.StudentInfoResponse;
import com.project.schoolmanagment.service.business.StudentInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/studentInfo")
@RequiredArgsConstructor
public class StudentInfoController {

    private final StudentInfoService studentInfoService;


    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public ResponseMessage<StudentInfoResponse> saveStudentInfo(
            HttpServletRequest request,
            @RequestBody @Valid StudentInfoRequest studentInfoRequest
    ) {
        return studentInfoService.saveStudentInfo(request, studentInfoRequest);
    }

    @DeleteMapping("/delete/{studentInfoId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseMessage delete(@PathVariable Long studentInfoId) {
        return studentInfoService.deleteById(studentInfoId);
    }

    @GetMapping("/getAllStudentsInfoByPage")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER','TEACHER')")
    public Page<StudentInfoResponse> getAllStudentInfoByPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "absentee") String sort,
            @RequestParam(value = "type", defaultValue = "desc") String type
    ) {
        return studentInfoService.getStudentInfoByPage(page, size, sort, type);
    }

    @PutMapping("/update/{studentInfoId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public ResponseMessage<StudentInfoResponse> update(
            @RequestBody @Valid UpdateStudentInfoRequest studentInfoRequest,
            @PathVariable Long studentInfoId
    ) {
        return studentInfoService.update(studentInfoRequest, studentInfoId);
    }

    //teacher wants to get his/her students' info
    @GetMapping("/getAllForTeacher")
    @PreAuthorize("hasAnyAuthority('TEACHER')")
    public ResponseEntity<Page<StudentInfoResponse>> getAllForTeacher(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(studentInfoService.getAllForTeacher(request, page, size), HttpStatus.OK);
    }

    //student wants to get his/her students' info
    @GetMapping("/getAllForStudent")
    @PreAuthorize("hasAnyAuthority('STUDENT')")
    public ResponseEntity<Page<StudentInfoResponse>> getAllForStudent(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return new ResponseEntity<>(studentInfoService.getAllForStudent(request, page, size), HttpStatus.OK);
    }

    @GetMapping("/getByStudentId/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<List<StudentInfoResponse>> getStudentInfoByStudentId(@PathVariable Long studentId) {
        List<StudentInfoResponse> studentInfoResponses = studentInfoService.getStudentInfoByStudentId(studentId);
        return ResponseEntity.ok(studentInfoResponses);
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','ASSISTANT_MANAGER')")
    public ResponseEntity<StudentInfoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentInfoService.findById(id));
    }


}
