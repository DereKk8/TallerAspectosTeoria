package com.ejemplo.notasapp.controlador;

import com.ejemplo.notasapp.modelo.Nota;
import com.ejemplo.notasapp.modelo.Estudiante;
import com.ejemplo.notasapp.repositorio.RepositorioNota;
import com.ejemplo.notasapp.repositorio.RepositorioEstudiante;
import com.ejemplo.notasapp.servicio.ServicioNota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/notas")
public class NotaController {

    @Autowired
    private RepositorioNota notaRepo;

    @Autowired
    private RepositorioEstudiante estudianteRepo;

    @Autowired
    private ServicioNota servicioNota;

    @GetMapping("/{estudianteId}")
    public String listar(@PathVariable Long estudianteId, Model model) {
        model.addAttribute("notas", notaRepo.findByEstudianteId(estudianteId));
        model.addAttribute("estudianteId", estudianteId);
        Estudiante est = estudianteRepo.findById(estudianteId).orElseThrow();
        model.addAttribute("estudianteNombre", est.getNombre());
        model.addAttribute("estudianteApellido", est.getApellido());
        return "notas";
    }

    @GetMapping("/{estudianteId}/nueva")
    public String nueva(@PathVariable Long estudianteId, Model model) {
        Nota nota = new Nota();
        nota.setEstudiante(estudianteRepo.findById(estudianteId).orElseThrow());
        model.addAttribute("nota", nota);
        return "editar-nota";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Nota nota) {
        notaRepo.save(nota);
        return "redirect:/notas/" + nota.getEstudiante().getId();
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("nota", notaRepo.findById(id).orElseThrow());
        return "editar-nota";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        Nota nota = notaRepo.findById(id).orElseThrow();
        Long estudianteId = nota.getEstudiante().getId();
        notaRepo.delete(nota);
        return "redirect:/notas/" + estudianteId;
    }

    @GetMapping("/{estudianteId}/promedio")
    public String mostrarFormularioPromedio(@PathVariable Long estudianteId, Model model) {
        model.addAttribute("estudianteId", estudianteId);
        model.addAttribute("materia", "");
        model.addAttribute("promedio", null);
        return "nota-promedio";
    }

    @PostMapping("/promedio")
    public String calcularPromedio(@RequestParam Long estudianteId,
            @RequestParam String materia,
            Model model) {
        double promedio = servicioNota.calcularPromedioPorMateria(estudianteId, materia);
        model.addAttribute("promedio", promedio);
        model.addAttribute("estudianteId", estudianteId);
        model.addAttribute("materia", materia);
        return "nota-promedio";
    }

}