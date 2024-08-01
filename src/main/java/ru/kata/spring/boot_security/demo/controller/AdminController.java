package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.exception.UserNotFoundException;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import ru.kata.spring.boot_security.demo.service.RoleService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String getAdminPage(Model model) {
        List<User> users = userService.findAllUsers();
        Map<Long, String> userRolesMap = users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user.getRoles().stream()
                                .map(Role::getRole)
                                .collect(Collectors.joining(", "))
                ));

        if (userRolesMap.isEmpty()) {
            System.out.println("userRolesMap is empty!");
        } else {
            System.out.println("userRolesMap is populated.");
        }

        model.addAttribute("users", users);
        model.addAttribute("userRolesMap", userRolesMap);
        return "admin";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", roleService.findAllRoles());
        return "edit_user";
    }

    @PatchMapping("/{id}")
    public String updateUser(@ModelAttribute("user") User user, @PathVariable("id") Long id, @RequestParam("roles") Set<Long> roleIds) {
        if (userService.findById(user.getId()) == null) {
            throw new UserNotFoundException("User with ID " + user.getId() + " not found.");
        }
        Set<Role> roles = roleService.findRolesByIds(roleIds);
        userService.updateUserRoles(id, roles);
        userService.updateUser(id, user);
        return "redirect:/admin";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        if (userService.findById(id) == null) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userService.deleteUserById(id);
        return "redirect:/admin";
    }

    private Map<Long, String> getUserRolesMap(List<User> users) {
        return users.stream().collect(Collectors.toMap(
                User::getId,
                user -> user.getRoles().stream().map(Role::getRole).collect(Collectors.joining(", "))
        ));
    }
}
