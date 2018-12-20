package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.User;
import com.paw.bettertrello.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @RequestMapping(method=RequestMethod.GET, value="/users/notifications")
    public ResponseEntity<?> getUserNotifications(Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new ResponseEntity<>(user.getNotifications(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @RequestMapping(method= RequestMethod.POST, value="/users")
    public ResponseEntity<?> postUser(@RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return new ResponseEntity<>("Empty username", HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return new ResponseEntity<>("Password must contain at least 6 characters", HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());

        if (optionalUser.isPresent()) {
            return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
        }
        else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if(user.getNotifications() == null){
                user.setNotifications(new ArrayList<>());
            }
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
        }
    }

    @RequestMapping(method=RequestMethod.GET, value="/users/notifications")
    public ResponseEntity<?> getUserNotifications(Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new ResponseEntity<>(user.getNotifications(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

   @RequestMapping(method= RequestMethod.DELETE, value="/users/notifications")
    public ResponseEntity<?> deleteUserNotifications(Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (optionalUser.isPresent()) {
           User user = optionalUser.get();
           user.getNotifications().clear();
           return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
       } else {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
   }
}