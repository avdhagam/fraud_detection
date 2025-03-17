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
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;



    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(
            @RequestParam String name,
            @RequestParam String phone) {

        UserEntity user = userService.registerUser(name, phone);
        return ResponseEntity.ok(user);
    }

    // Fetch user details
    @GetMapping("/{userId}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String userId) {
        Optional<UserEntity> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }




    @GetMapping("/{userId}/pdf")
    public ResponseEntity<byte[]> generateUserPdf(@PathVariable String userId) {
        try {
            //  Step 1: Fetch User Data from API (GET /users/{userId})
            String userApiUrl = "http://localhost:8080/users/" + userId;  // Update with actual URL
            ResponseEntity<String> userDataResponse = restTemplate.getForEntity(userApiUrl, String.class);

            if (userDataResponse.getStatusCode() != HttpStatus.OK) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Step 2: Parse JSON Response
            JsonNode userData = objectMapper.readTree(userDataResponse.getBody());

            // Step 3: Generate PDF
            byte[] pdfBytes = PdfGeneratorUtil.generateUserPdf(userData);

            //  Step 4: Send Response as PDF
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=user_" + userId + "_report.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
