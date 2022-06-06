package com.fc.dMaker.controller;

import com.fc.dMaker.dto.*;
import com.fc.dMaker.exception.DMakerException;
import com.fc.dMaker.service.DMakerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class DMakerController {

    private final DMakerService dMakerService;

    @GetMapping("/developers")
    public List<DeveloperDto> getAllDevelopers() {
        log.info("GET /developers HTTP/1.1");

        return dMakerService.getAllEmployedDevelopers();
    }

    @GetMapping("/developer/{memberId}")
    public DeveloperDetailDto getDeveloperDetail(@PathVariable String memberId) {
        log.info("GET /developer detail HTTP/1.1");

        return dMakerService.getDeveloperDetail(memberId);
    }

    @PostMapping("/create-developer")
    public CreateDeveloper.Response createDevelopers(@Valid @RequestBody CreateDeveloper.Request request) {
        log.info("request : {}", request);

        return dMakerService.createDeveloper(request);

    }

    @PutMapping("/developer/{memberId}")
    public DeveloperDetailDto editDeveloper(
            @PathVariable String memberId,
            @Valid @RequestBody EditDeveloper.Request request
            ) {
        log.info("request : {}", request);

        return dMakerService.editDeveloper(memberId, request);
    }

    @DeleteMapping("/developer/{memberId}")
    public DeveloperDetailDto deleteDeveloper(@PathVariable String memberId) {
        return dMakerService.deleteDeveloper(memberId);
    }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ExceptionHandler(DMakerException.class)
    public DMakerErrorResponse handleException(DMakerException e, HttpServletRequest request) {
        log.info("error Code: {}, url: {}, message: {}",
                e.getDMakerErrorCode(), request.getRequestURL(), e.getDetailMessage());

        return DMakerErrorResponse.builder()
                .errorCode(e.getDMakerErrorCode())
                .errorMessage(e.getDetailMessage())
                .build();
    }
}