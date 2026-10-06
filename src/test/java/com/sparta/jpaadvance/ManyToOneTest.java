package com.sparta.jpaadvance;

import com.sparta.jpaadvance.entity.Food;
import com.sparta.jpaadvance.entity.User;
import com.sparta.jpaadvance.repository.FoodRepository;
import com.sparta.jpaadvance.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
public class ManyToOneTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    @Rollback(value = false)
    @DisplayName("N대1 저장 : 같은 고객의 음식 두 개")
    void test1() {
        List<Food> savedFoods = saveFoodsForOneUser();
        Long userId = savedFoods.get(0).getUser().getId();
        entityManager.flush();
        entityManager.clear();

        for (Food savedFood : savedFoods) {
            Food food = foodRepository.findById(savedFood.getId()).orElseThrow();
            assertEquals(userId, food.getUser().getId());
        }
    }

    @Test
    @DisplayName("N대1 조회 : Food 기준 user 정보 조회")
    void test5() {
        List<Food> savedFoods = saveFoodsForOneUser();
        Long foodId = savedFoods.get(0).getId();
        Long userId = savedFoods.get(0).getUser().getId();
        entityManager.flush();
        entityManager.clear();

        Food food = foodRepository.findById(foodId).orElseThrow();
        System.out.println("food.getName() = " + food.getName());
        System.out.println("food.getUser().getName() = " + food.getUser().getName());
        assertEquals(userId, food.getUser().getId());
        assertEquals("Robbie", food.getUser().getName());
    }

    @Test
    @DisplayName("N대1 조회 : User 기준 food 정보 조회")
    void test6() {
        List<Food> savedFoods = saveFoodsForOneUser();
        Long userId = savedFoods.get(0).getUser().getId();
        entityManager.flush();
        entityManager.clear();

        User user = userRepository.findById(userId).orElseThrow();
        System.out.println("user.getName() = " + user.getName());
        List<Food> foodList = user.getFoodList();
        for (Food food : foodList) {
            System.out.println("food.getName() = " + food.getName());
            System.out.println("food.getPrice() = " + food.getPrice());
        }
        assertEquals(2, foodList.size());
        assertTrue(foodList.stream().allMatch(food -> userId.equals(food.getUser().getId())));
    }

    private List<Food> saveFoodsForOneUser() {
        User user = new User();
        user.setName("Robbie");

        Food food = new Food();
        food.setName("후라이드 치킨");
        food.setPrice(15000);
        food.setUser(user); // 외래 키(연관 관계) 설정

        Food food2 = new Food();
        food2.setName("양념 치킨");
        food2.setPrice(20000);
        food2.setUser(user); // 외래 키(연관 관계) 설정

        // 양쪽 객체 참조도 같은 관계를 가리키도록 맞춘다.
        user.getFoodList().addAll(List.of(food, food2));
        userRepository.save(user);
        return foodRepository.saveAll(List.of(food, food2));
    }
}
