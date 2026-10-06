package com.sparta.jpaadvance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "food")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    // 이전 강의: 직접 @ManyToMany 매핑 (현재는 Order 엔티티로 연결)
    // @ManyToMany
    // @JoinTable(name = "orders",
    //         joinColumns = @JoinColumn(name = "food_id"),
    //         inverseJoinColumns = @JoinColumn(name = "user_id"))
    // private List<User> userList = new ArrayList<>();
    //
    // public void addUserList(User user) {
    //     this.userList.add(user);
    // }

    @OneToMany(mappedBy = "food")
    private List<Order> orderList = new ArrayList<>();
}
