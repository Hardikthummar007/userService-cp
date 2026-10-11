package com.example.userService.feign;


import com.example.userService.dto.codeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "compilerService",path = "/compilerService")
public interface compilerClient {

    @PostMapping("runCode")
    ResponseEntity<?> runcode(@RequestBody codeDto codeDto);

}
