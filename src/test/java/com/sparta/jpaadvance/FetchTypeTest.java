package com.sparta.jpaadvance;

import com.sparta.jpaadvance.entity.Food;
import com.sparta.jpaadvance.entity.User;
import com.sparta.jpaadvance.repository.FoodRepository;
import com.sparta.jpaadvance.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FetchTypeTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    FoodRepository foodRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    @Rollback(value = false)
    void init() {
        List<User> userList = new ArrayList<>();
        User user1 = new User();
        user1.setName("Robbie");
        userList.add(user1);

        User user2 = new User();
        user2.setName("Robbert");
        userList.add(user2);
        userRepository.saveAll(userList);

        List<Food> foodList = new ArrayList<>();
        Food food1 = new Food();
        food1.setName("고구마 피자");
        food1.setPrice(30000);
        food1.setUser(user1); // 외래 키(연관 관계) 설정
        foodList.add(food1);

        Food food2 = new Food();
        food2.setName("아보카도 피자");
        food2.setPrice(50000);
        food2.setUser(user1); // 외래 키(연관 관계) 설정
        foodList.add(food2);

        Food food3 = new Food();
        food3.setName("후라이드 치킨");
        food3.setPrice(15000);
        food3.setUser(user1); // 외래 키(연관 관계) 설정
        foodList.add(food3);

        Food food4 = new Food();
        food4.setName("후라이드 치킨");
        food4.setPrice(15000);
        food4.setUser(user2); // 외래 키(연관 관계) 설정
        foodList.add(food4);

        Food food5 = new Food();
        food5.setName("고구마 피자");
        food5.setPrice(30000);
        food5.setUser(user2); // 외래 키(연관 관계) 설정
        foodList.add(food5);
        foodRepository.saveAll(foodList);
    }

    @Test
    @DisplayName("아보카도 피자 조회")
    void test1() {
        Food sample = saveAvocadoForRobbie();
        Food food = foodRepository.findById(sample.getId()).orElseThrow();
        assertEquals("아보카도 피자", food.getName());
        assertNotNull(food.getUser(), "조회할 음식에는 고객이 연결되어 있어야 합니다.");

        System.out.println("food.getName() = " + food.getName());
        System.out.println("food.getPrice() = " + food.getPrice());

        System.out.println("아보카도 피자를 주문한 회원 정보 조회");
        System.out.println("food.getUser().getName() = " + food.getUser().getName());
        assertEquals("Robbie", food.getUser().getName());
    }

    @Test
    @Transactional
    @DisplayName("Robbie 고객 조회")
    void test2() {
        Food sample = saveAvocadoForRobbie();
        Long userId = sample.getUser().getId();
        entityManager.flush();
        entityManager.clear(); // 저장할 때 사용한 객체 대신 DB에서 다시 조회합니다.

        User user = userRepository.findById(userId).orElseThrow();
        var loadState = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        assertFalse(loadState.isLoaded(user, "foodList"));
        System.out.println("user.getName() = " + user.getName());

        System.out.println("Robbie가 주문한 음식 이름 조회");
        for (Food food : user.getFoodList()) {
            System.out.println(food.getName());
        }
        assertTrue(loadState.isLoaded(user, "foodList"));
        assertEquals(1, user.getFoodList().size());
        assertEquals("아보카도 피자", user.getFoodList().get(0).getName());
    }

    @Test
    @DisplayName("Robbie 고객 조회 실패")
    void test3() {
        Food sample = saveAvocadoForRobbie();
        User user = userRepository.findById(sample.getUser().getId()).orElseThrow();
        System.out.println("user.getName() = " + user.getName());

        System.out.println("Robbie가 주문한 음식 이름 조회");
        // 테스트에 @Transactional이 없어 repository 조회 뒤 컨텍스트가 종료됩니다.
        // 실패 자체가 학습 목적이므로 예외 발생을 검증합니다.
        LazyInitializationException exception = assertThrows(LazyInitializationException.class, () -> {
            for (Food food : user.getFoodList()) {
                System.out.println(food.getName());
            }
        });
        System.out.println("예상한 지연 로딩 예외 = " + exception.getMessage());
    }

    // 조회 테스트를 하나만 실행해도 기존 DB의 ID나 이름 중복에 영향을 받지 않습니다.
    private Food saveAvocadoForRobbie() {
        User user = new User();
        user.setName("Robbie");
        userRepository.save(user);

        Food food = new Food();
        food.setName("아보카도 피자");
        food.setPrice(50000);
        food.setUser(user);
        return foodRepository.save(food);
    }

}