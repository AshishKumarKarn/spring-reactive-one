package com.karn.projects.dao;

import com.karn.projects.document.Employee;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ReactiveDao extends ReactiveMongoRepository<Employee, String> {


}
