package com.example.user;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;

import java.util.Optional;

@Controller("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Get
    public Iterable<User> findAll() {
        return userService.findAll();
    }

    @Get("/{id}")
    public HttpResponse<User> findById(Long id) {
        return userService.findById(id)
            .map(HttpResponse::ok)
            .orElseGet(HttpResponse::notFound);
    }

    @Post
    public HttpResponse<User> create(@Body User user) {
        return HttpResponse.created(userService.create(user));
    }

    @Put("/{id}")
    public HttpResponse<User> update(Long id, @Body User user) {
        Optional<User> updated = userService.update(id, user);
        return updated.map(HttpResponse::ok)
            .orElseGet(HttpResponse::notFound);
    }

    @Delete("/{id}")
    public HttpResponse<?> delete(Long id) {
        return userService.delete(id)
            ? HttpResponse.noContent()
            : HttpResponse.notFound();
    }
}
