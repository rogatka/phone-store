package com.epam.store.controller.rest;

import com.epam.store.config.WebJavaConfig;
import com.epam.store.entity.Account;
import com.epam.store.entity.Order;
import com.epam.store.entity.OrderStatus;
import com.epam.store.entity.User;
import com.epam.store.service.AccountService;
import com.epam.store.service.OrderService;
import com.epam.store.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        WebJavaConfig.class
})
@WebAppConfiguration
@EnableWebMvc
@TestPropertySource("classpath:test-persistence.properties")
public class TestUserController {
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        ScriptUtils.executeSqlScript(dataSource.getConnection(),
                new EncodedResource(new ClassPathResource("create-tables.sql"), StandardCharsets.UTF_8));
        ScriptUtils.executeSqlScript(
                dataSource.getConnection(),
                new EncodedResource(new ClassPathResource("populate-tables.sql"), StandardCharsets.UTF_8));
    }

    @AfterEach
    public void tearDown() throws Exception {
        ScriptUtils.executeSqlScript(
                dataSource.getConnection(),
                new EncodedResource(new ClassPathResource("drop-tables.sql"), StandardCharsets.UTF_8));
    }

    @Test
    public void getUsersShouldReturn200() throws Exception {
        int expectedValue = 1;
        String expectedFirstName = "Michael";
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(expectedValue))
                .andExpect(jsonPath("$[0].firstName").value(expectedFirstName))
                .andDo(print());
    }

    @Test
    public void getUsersIfNoUsersFoundShouldReturnEmptyListAndStatus200() throws Exception {
        ScriptUtils.executeSqlScript(
                dataSource.getConnection(),
                new EncodedResource(new ClassPathResource("delete-tables.sql"), StandardCharsets.UTF_8));
        mockMvc.perform(get("/users"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUsersSortByFirstNameShouldReturnSortedUsers() throws Exception {
        String sortByFirstName = "firstName";
        String expectedFirstName = "Chael";
        mockMvc.perform(get("/users?sortBy={firstName}", sortByFirstName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].firstName").value(expectedFirstName))
                .andDo(print());
    }

    @Test
    public void getUsersSortByLastNameShouldReturnSortedUsers() throws Exception {
        String sortByLastName = "lastName";
        String expectedLastName = "Cordon";
        mockMvc.perform(get("/users?sortBy={lastName}", sortByLastName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].lastName").value(expectedLastName))
                .andDo(print());
    }

    @Test
    public void getUsersSearchByFirstNameShouldReturnMatchedUser() throws Exception {
        String searchName = "hael";
        String expectedFirstName = "Chael";
        mockMvc.perform(get("/users?name={name}", searchName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value(expectedFirstName))
                .andDo(print());
    }

    @Test
    public void getUsersSearchByLastNameShouldReturnMatchedUser() throws Exception {
        String searchName = "don";
        String expectedLastName = "Gordon";
        mockMvc.perform(get("/users?name={name}", searchName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName").value(expectedLastName))
                .andDo(print());
    }

    @Test
    public void getUserByIdShouldReturn200IfFound() throws Exception {
        int id = 3;
        mockMvc.perform(get("/users/{id}", id))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andDo(print());
    }

    @Test
    public void getUserByIdShouldReturn404IfNotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 10))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void updateUserShouldReturn200IfValid() throws Exception {
        Long id = 2L;
        User user = userService.findById(id).get();
        user.setFirstName("Billy");
        ObjectMapper objectMapper = new ObjectMapper();
        String updateUserJson = objectMapper.writeValueAsString(user);
        mockMvc.perform(put("/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateUserJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void createUserShouldReturn200IfValid() throws Exception {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        ObjectMapper objectMapper = new ObjectMapper();
        String createUserJson = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void createUserShouldReturn400IfUserIsNull() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String userJson = objectMapper.writeValueAsString(null);
        mockMvc.perform(post("/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void deleteUserShouldReturn200IfNoAccount() throws Exception {
        Account account = accountService.findByUserId(1L).get();
        List<Order> orders = orderService.findAllByAccountId(account.getId());
        for (Order order: orders) {
            order.setStatus(OrderStatus.READY);
            order.setTotalSum(BigDecimal.ONE);
            orderService.save(order);
        }
        accountService.deleteById(account.getId());
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void deleteUserShouldReturn400IfThereIsAccountWithThatUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void deleteUserShouldReturn404IfNotFound() throws Exception {
        mockMvc.perform(delete("/accounts/5"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
