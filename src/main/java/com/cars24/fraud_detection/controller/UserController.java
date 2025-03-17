package com.cars24.fraud_detection.controller;

import com.cars24.fraud_detection.data.entity.AudioEntity;
import com.cars24.fraud_detection.data.entity.UserEntity;
import com.cars24.fraud_detection.service.AudioService;
import com.cars24.fraud_detection.service.UserService;
import com.cars24.fraud_detection.utils.PdfGeneratorUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
//    private final ObjectMapper objectMapper;
//    private final RestTemplate restTemplate;

    //register user
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(
            @RequestParam String name,
            @RequestParam String phone) {

        UserEntity user = userService.registerUser(name, phone);
        return ResponseEntity.ok(user);
    }

    //fetch user-details
    @GetMapping("/{userId}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String userId) {
        Optional<UserEntity> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //generate user-details pdf
    @GetMapping("/{userId}/pdf")
    public ResponseEntity<byte[]> generateUserPdf(@PathVariable String userId) {
        try {
            byte[] pdfBytes = userService.generateUserPdf(userId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=user_" + userId + "_report.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
