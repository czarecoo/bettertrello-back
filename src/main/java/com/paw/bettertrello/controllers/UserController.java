package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.User;
import com.paw.bettertrello.repositories.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(description="Operations pertaining to users in application")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @ApiOperation(value = "Add a user")
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

    @ApiOperation(value = "Add or change avator for user")
    @RequestMapping(method= RequestMethod.POST, value="/user/avatar")
    public ResponseEntity<?> postUserAvatar(@RequestBody String avatar, Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setAvatar(avatar);
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Change user password")
    @RequestMapping(method= RequestMethod.POST, value="/user/password")
    public ResponseEntity<?> postUserPassword(@RequestBody String password, Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Search for user", response = User.class)
    @RequestMapping(method=RequestMethod.GET, value="/user/")
    public ResponseEntity<?> getUser(Principal principal) {
        String userName = principal.getName();
        Optional<User> optionalUser = userRepository.findByUsernameExcludingSensitiveData(userName);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Delete user notifications", response = User.class)
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