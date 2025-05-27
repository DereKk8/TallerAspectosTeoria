package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.modelo.Estudiante;
import com.ejemplo.notasapp.repositorio.RepositorioEstudiante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private RepositorioEstudiante estudianteRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", estudianteRepo.findAll());
        return "estudiantes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "editar-estudiante";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Estudiante estudiante) {
        estudianteRepo.save(estudiante);
        return "redirect:/estudiantes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("estudiante", estudianteRepo.findById(id).orElseThrow());
        return "editar-estudiante";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        estudianteRepo.deleteById(id);
        return "redirect:/estudiantes";
    }
}