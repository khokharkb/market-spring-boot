package org.example.market.security;

import org.example.market.user.DTO.RegistrationRequest;
import org.example.market.user.User;
import org.example.market.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "capacitor://localhost", "http://localhost"})
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute RegistrationRequest request,
                               BindingResult result,
                               Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "Cet email est déjà utilisé");
            return "register";
        }

        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "Ce nom d'utilisateur est déjà pris");
            return "register";
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setTelephone(request.getTelephone());
            user.setAdresse(request.getAdresse());
            user.setVille(request.getVille());
            user.setActive(true);
            user.setRole("USER");

            userRepository.save(user);

            return "redirect:/login?registered";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de l'inscription : " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}