package vn.vku.udn.hienpc.bmichatbot.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.vku.udn.hienpc.bmichatbot.entity.Exercise;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.ExerciseRepository;
import vn.vku.udn.hienpc.bmichatbot.service.AuditLogService;
import vn.vku.udn.hienpc.bmichatbot.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/exercises")
public class AdminExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminExerciseController(ExerciseRepository exerciseRepository,
                                     UserService userService,
                                     AuditLogService auditLogService) {
        this.exerciseRepository = exerciseRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listExercises(Model model) {
        List<Exercise> exercises = exerciseRepository.findAll();
        model.addAttribute("exercises", exercises);
        return "admin/manage-exercises";
    }

    @PostMapping("/add")
    public String addExercise(@RequestParam String exerciseName,
                              @RequestParam Integer caloriesBurnedPerHour,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(userDetails.getUsername());

            Exercise exercise = new Exercise();
            exercise.setExerciseName(exerciseName);
            exercise.setCaloriesBurnedPerHour(caloriesBurnedPerHour);

            Exercise savedExercise = exerciseRepository.save(exercise);

            // Ghi audit log
            auditLogService.logCreate(admin, "Exercise",
                    String.valueOf(savedExercise.getExerciseId()),
                    String.format("Name: %s, Calories/Hour: %d", exerciseName, caloriesBurnedPerHour));

            redirectAttributes.addFlashAttribute("successMessage", "Bài tập đã được thêm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm bài tập: " + e.getMessage());
        }

        return "redirect:/admin/exercises";
    }
}

