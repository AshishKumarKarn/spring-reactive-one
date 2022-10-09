package com.karn.projects.controller;

import com.karn.projects.dao.ReactiveDao;
import com.karn.projects.document.Employee;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@Log4j2
public class MyReactiveController {

    @Autowired
    private ReactiveDao dao;

    @RequestMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @RequestMapping(value = "/strings", method = RequestMethod.GET, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<EmployeeData> getStringsStream() {
        return Flux.just(new EmployeeData("Alok",Thread.currentThread().getName()),
                new EmployeeData("Ashish",Thread.currentThread().getName()),
                new EmployeeData("Neeraj",Thread.currentThread().getName())
                ,new EmployeeData("Shantanu",Thread.currentThread().getName()),
                new EmployeeData("Gaurav",Thread.currentThread().getName())).delayElements(Duration.ofSeconds(2));
    }

record EmployeeData(String name, String threadName){}

    @RequestMapping(value = "/addEmp/{name}/{age}", method = RequestMethod.GET)
    public Mono<String> addEmployee(@PathVariable("name") String name, @PathVariable("age") int age) {
        Employee emp = new Employee(null, name, age);
        return dao.save(emp).map(Employee::getId);
    }

    @RequestMapping(value = "/getEmp", method = RequestMethod.GET)
    public Flux<Employee> getEmployee() {
        return dao.findAll();
    }
}
