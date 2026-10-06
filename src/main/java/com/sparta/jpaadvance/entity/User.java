package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // 이전 강의: 직접 @ManyToMany 양방향 매핑 (Food.userList를 가리킴)
    // @ManyToMany(mappedBy = "userList")
    // private List<Food> foodList = new ArrayList<>();
    //
    // public void addFoodList(Food food) {
    //     this.foodList.add(food);
    //     food.getUserList().add(this);
    // }

    @OneToMany(mappedBy = "user")
    private List<Food> foodList = new ArrayList<>();
}
