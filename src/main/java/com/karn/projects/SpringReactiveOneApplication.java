package com.karn.projects;

import com.karn.projects.dao.ReactiveDao;
import com.karn.projects.document.Employee;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class SpringReactiveOneApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringReactiveOneApplication.class, args);
    }

}

/******************** USING FUNCTION STYLE WAY OR EXPOSING REST POINTS********************************/
/**
 * ACCESS : http://localhost:8080/all
 * */
@Configuration
class FunctionalReactiveStyleHTTPConfig {

    @Bean
    RouterFunction<ServerResponse> routes(ReactiveDao repo) {
        return route()
                .GET("/all", request -> ok().body(repo.findAll(), Employee.class))
                //.POST().DELETE()//OTHER ROUTES
                .build();
    }
}

/********************************Above program ends************************************/

/**
 * Below program demonstrates a websocket exposed API to send greetings at every second
 * The input name is taken through ws.html
 * Access: http://localhost:8080/ws.html see console logs.
 * The code is still non-blocking and the same thread sending greetings can be reused to serve
 * other requests in between serving greeting messages.
 * Below code can be refactored to use method references or lamba however
 * I have kept it as it for better understanding.
 * */
@Configuration
class WebSocketConfiguration {

    @Bean
    SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler wsh) {
        return new SimpleUrlHandlerMapping() {
            {
                setUrlMap(Map.of("/ws/greetings", wsh));
                setOrder(10);
            }
        };
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler(GreetingProducer gp) {
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            @Override
            public Mono<Void> handle(WebSocketSession session) {
                Flux<WebSocketMessage> response = session
                        .receive()
                        .map(wsm -> wsm.getPayloadAsText())
                        .map(name -> new GreetingRequest(name))
                        .flatMap(greetingRequest -> gp.greet(greetingRequest))
                        .map(greetingResponse -> greetingResponse.getMessage())
                        .map(message -> session.textMessage(message));
                return session.send(response);
            }
        };
        return webSocketHandler;
    }
}

@Data
class GreetingRequest {
    private String name;

    public GreetingRequest(String name) {
        this.name = name;
    }
}

@Data
class GreetingResponse {
    private String message;

    public GreetingResponse(String message) {
        this.message = message;
    }
}

@Component
class GreetingProducer {
    Flux<GreetingResponse> greet(GreetingRequest greetingRequest) {

        return Flux.fromStream(Stream.generate(new Supplier<GreetingResponse>() {
                    @Override
                    public GreetingResponse get() {
                        return new GreetingResponse("Hello " + greetingRequest.getName() + " @" + Instant.now());
                    }
                }))
                .delayElements(Duration.ofSeconds(1));
    }
}
