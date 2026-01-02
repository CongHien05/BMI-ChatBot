package vn.vku.udn.hienpc.bmichatbot.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AdminDashboardResponse;
import vn.vku.udn.hienpc.bmichatbot.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        AdminDashboardResponse stats = adminService.getDashboardStats(userDetails.getUsername());
        model.addAttribute("stats", stats);
        model.addAttribute("adminEmail", userDetails.getUsername());
        return "admin/dashboard";
    }
}
