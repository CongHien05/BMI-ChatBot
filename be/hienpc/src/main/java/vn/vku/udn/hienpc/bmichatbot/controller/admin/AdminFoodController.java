package vn.vku.udn.hienpc.bmichatbot.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.FoodRepository;
import vn.vku.udn.hienpc.bmichatbot.service.AuditLogService;
import vn.vku.udn.hienpc.bmichatbot.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/foods")
public class AdminFoodController {

    private final FoodRepository foodRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminFoodController(FoodRepository foodRepository,
                                UserService userService,
                                AuditLogService auditLogService) {
        this.foodRepository = foodRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listFoods(Model model) {
        List<Food> foods = foodRepository.findAll();
        model.addAttribute("foods", foods);
        return "admin/manage-foods";
    }

    @PostMapping("/add")
    public String addFood(@RequestParam String foodName,
                          @RequestParam String servingUnit,
                          @RequestParam Integer caloriesPerUnit,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(userDetails.getUsername());

            Food food = new Food();
            food.setFoodName(foodName);
            food.setServingUnit(servingUnit);
            food.setCaloriesPerUnit(caloriesPerUnit);

            Food savedFood = foodRepository.save(food);

            // Ghi audit log
            auditLogService.logCreate(admin, "Food",
                    String.valueOf(savedFood.getFoodId()),
                    String.format("Name: %s, Unit: %s, Calories: %d", foodName, servingUnit, caloriesPerUnit));

            redirectAttributes.addFlashAttribute("successMessage", "Món ăn đã được thêm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm món ăn: " + e.getMessage());
        }

        return "redirect:/admin/foods";
    }
}
