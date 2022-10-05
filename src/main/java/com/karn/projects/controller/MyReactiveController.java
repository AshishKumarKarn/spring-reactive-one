package com.karn.projects.controller;

import com.karn.projects.dao.ReactiveDao;
import com.karn.projects.document.Employee;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String hello(){
        return "Hello";
    }

    @RequestMapping("/strings")
    public Flux<String> getStringsStream(){
        return Flux.just("Sukanya", "Ashish", "Akash", "Aditya", "Atmika").delayElements(Duration.ofSeconds(5));
    }

    @RequestMapping(value = "/addEmp/{name}/{age}",method = RequestMethod.GET)
    public Mono<String> addEmployee(@PathVariable("name") String name,@PathVariable("age") int age){
        Employee emp=new Employee(null,name,age);
        return dao.save(emp).map(Employee::getId);
    }

    @RequestMapping(value = "/getEmp",method = RequestMethod.GET)
    public Flux<Employee> getEmployee(){
        return dao.findAll();
    }
}
