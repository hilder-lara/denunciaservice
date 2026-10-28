package com.lara.denunciaservice.controller;

import com.lara.denunciaservice.dto.CrearDenunciaRequest;
import com.lara.denunciaservice.entity.Denuncia;
import com.lara.denunciaservice.service.DenunciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/denuncias")
public class DenunciaController {
    private final DenunciaService service;

    public DenunciaController(DenunciaService service) {
        this.service = service;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<Denuncia> registrar(@Valid @RequestBody CrearDenunciaRequest request) {
        Denuncia denuncia = service.registrar(request);
        return ResponseEntity.created(URI.create("/denuncias/" + denuncia.getId())).body(denuncia);
    }

    @GetMapping({"", "/"})
    public List<Denuncia> listar(@RequestParam(defaultValue = "0") int offset,
                                 @RequestParam(defaultValue = "5") int limit) {
        return service.listar(offset, limit);
    }

    @GetMapping({"/{id}", "/{id}/"})
    public Denuncia obtener(@PathVariable int id) {
        return service.obtener(id);
    }

    @GetMapping({"/ciudadano/{dni}", "/ciudadano/{dni}/"})
    public List<Denuncia> buscarPorDni(@PathVariable String dni) {
        return service.buscarPorDni(dni);
    }

    @PutMapping({"/{id}", "/{id}/"})
    public Denuncia atender(@PathVariable int id) {
        return service.atender(id);
    }
}
