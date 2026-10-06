package com.sparta.jpaadvance.cascade;

import com.sparta.jpaadvance.entity.Food;
import com.sparta.jpaadvance.entity.User;
import com.sparta.jpaadvance.repository.FoodRepository;
import com.sparta.jpaadvance.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CascadeTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("Robbie 음식 주문")
    void test1() {
        // 고객 Robbie 가 후라이드 치킨과 양념 치킨을 주문합니다.
        User user = new User();
        user.setName("Robbie");

        // 후라이드 치킨 주문
        Food food = new Food();
        food.setName("후라이드 치킨");
        food.setPrice(15000);

        user.addFoodList(food);

        Food food2 = new Food();
        food2.setName("양념 치킨");
        food2.setPrice(20000);

        user.addFoodList(food2);

        userRepository.save(user);
        foodRepository.save(food);
        foodRepository.save(food2);
    }

    @Test
    @DisplayName("영속성 전이 저장")
    void test2() {
        // 고객 Robbie 가 후라이드 치킨과 양념 치킨을 주문합니다.
        User user = new User();
        user.setName("Robbie");

        // 후라이드 치킨 주문
        Food food = new Food();
        food.setName("후라이드 치킨");
        food.setPrice(15000);

        user.addFoodList(food);

        Food food2 = new Food();
        food2.setName("양념 치킨");
        food2.setPrice(20000);

        user.addFoodList(food2);

        userRepository.save(user);

        // User만 저장해도 Food 두 개와 user_id 연결이 DB에 저장됐는지 확인합니다.
        assertNotNull(food.getId());
        assertNotNull(food2.getId());
        assertEquals(user.getId(), foodRepository.findById(food.getId()).orElseThrow().getUser().getId());
        assertEquals(user.getId(), foodRepository.findById(food2.getId()).orElseThrow().getUser().getId());
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("Robbie 탈퇴")
    void test3() {
        // 이번 테스트가 만든 고객만 삭제하며, 기존 DB의 중복 이름과 분리합니다.
        User savedUser = saveRobbieWithFoods();
        entityManager.flush();
        Long userId = savedUser.getId();
        List<Long> foodIds = savedUser.getFoodList().stream().map(Food::getId).toList();
        entityManager.clear();

        User user = userRepository.findById(userId).orElseThrow();
        System.out.println("user.getName() = " + user.getName());

        // Robbie 가 주문한 음식 조회
        for (Food food : user.getFoodList()) {
            System.out.println("food.getName() = " + food.getName());
        }

        // 주문한 음식 데이터 삭제
        foodRepository.deleteAll(user.getFoodList());

        // Robbie 탈퇴
        userRepository.delete(user);
        assertDeleted(userId, foodIds);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("영속성 전이 삭제")
    void test4() {
        // 이번 테스트가 만든 고객만 삭제하며, 기존 DB의 중복 이름과 분리합니다.
        User savedUser = saveRobbieWithFoods();
        entityManager.flush();
        Long userId = savedUser.getId();
        List<Long> foodIds = savedUser.getFoodList().stream().map(Food::getId).toList();
        entityManager.clear();

        User user = userRepository.findById(userId).orElseThrow();
        System.out.println("user.getName() = " + user.getName());

        // Robbie 가 주문한 음식 조회
        for (Food food : user.getFoodList()) {
            System.out.println("food.getName() = " + food.getName());
        }

        // Robbie 탈퇴
        userRepository.delete(user);
        assertDeleted(userId, foodIds);
    }

    private User saveRobbieWithFoods() {
        User user = new User();
        user.setName("Robbie");

        Food food = new Food();
        food.setName("후라이드 치킨");
        food.setPrice(15000);
        user.addFoodList(food);

        Food food2 = new Food();
        food2.setName("양념 치킨");
        food2.setPrice(20000);
        user.addFoodList(food2);

        return userRepository.save(user);
    }

    private void assertDeleted(Long userId, List<Long> foodIds) {
        entityManager.flush();
        entityManager.clear();
        assertFalse(userRepository.existsById(userId));
        for (Long foodId : foodIds) {
            assertFalse(foodRepository.existsById(foodId));
        }
    }

}
