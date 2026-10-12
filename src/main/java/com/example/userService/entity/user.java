package com.example.userService.entity;


import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class user {

    @Id
    String userName;

    String  password;
    String  role;
    String email;
    String coutry;
    int totalProblemSolved;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_problem_ids", joinColumns = @JoinColumn(name = "user_name"))
    @Column(name = "problem_id")
    Set<Integer> problemSolvedId=new HashSet<>();


    public Set<Integer> getProblemSolvedId() {
        return problemSolvedId;
    }

    public void setProblemSolvedId(Set<Integer> problemSolvedId) {
        this.problemSolvedId = problemSolvedId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCoutry() {
        return coutry;
    }

    public void setCoutry(String coutry) {
        this.coutry = coutry;
    }

    public int getTotalProblemSolved() {
        return totalProblemSolved;
    }

    public void setTotalProblemSolved(int totalProblemSolved) {
        this.totalProblemSolved = totalProblemSolved;
    }
}
