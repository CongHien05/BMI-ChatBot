package vn.vku.udn.hienpc.bmichatbot.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;
import vn.vku.udn.hienpc.bmichatbot.service.AuditLogService;
import vn.vku.udn.hienpc.bmichatbot.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/rules")
public class AdminChatbotRuleController {

    private final ChatbotRuleRepository chatbotRuleRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminChatbotRuleController(ChatbotRuleRepository chatbotRuleRepository,
                                       UserService userService,
                                       AuditLogService auditLogService) {
        this.chatbotRuleRepository = chatbotRuleRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listRules(Model model) {
        List<ChatbotRule> rules = chatbotRuleRepository.findAll();
        model.addAttribute("rules", rules);
        return "admin/manage-rules";
    }

    @PostMapping("/add")
    public String addRule(@RequestParam String intent,
                          @RequestParam String keywords,
                          @RequestParam String responseTemplate,
                          @RequestParam(defaultValue = "0") Integer priority,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByEmail(userDetails.getUsername());

            ChatbotRule rule = new ChatbotRule();
            rule.setIntent(intent);
            rule.setKeywords(keywords);
            rule.setResponseTemplate(responseTemplate);
            rule.setPriority(priority != null ? priority : 0);
            rule.setCreatedBy(admin);

            ChatbotRule savedRule = chatbotRuleRepository.save(rule);

            // Ghi audit log
            auditLogService.logCreate(admin, "ChatbotRule", 
                    String.valueOf(savedRule.getRuleId()),
                    String.format("Intent: %s, Keywords: %s", intent, keywords));

            redirectAttributes.addFlashAttribute("successMessage", "Rule đã được thêm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm rule: " + e.getMessage());
        }

        return "redirect:/admin/rules";
    }
}
