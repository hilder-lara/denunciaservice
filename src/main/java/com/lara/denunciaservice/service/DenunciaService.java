package com.lara.denunciaservice.service;

import com.lara.denunciaservice.dto.CrearDenunciaRequest;
import com.lara.denunciaservice.entity.Denuncia;
import com.lara.denunciaservice.exception.DenunciaNoEncontradaException;
import com.lara.denunciaservice.repository.DenunciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class DenunciaService {
    private final DenunciaRepository repository;

    public DenunciaService(DenunciaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Denuncia registrar(CrearDenunciaRequest request) {
        String tipo = request.tipo().trim().toUpperCase(Locale.ROOT);
        if (tipo.length() > 30) {
            throw new IllegalArgumentException("El tipo normalizado admite hasta 30 caracteres");
        }
        Denuncia denuncia = new Denuncia(
                request.dni(), request.fecha(), tipo,
                request.direccion().trim(), request.descripcion().trim());
        return repository.save(denuncia);
    }

    public List<Denuncia> listar(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset debe ser mayor o igual a 0");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit debe estar entre 1 y 100");
        }
        return repository.findPaginadas(offset, limit);
    }

    public Denuncia obtener(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("El código debe ser mayor a 0");
        }
        return repository.findById(id).orElseThrow(() -> new DenunciaNoEncontradaException(id));
    }

    public List<Denuncia> buscarPorDni(String dni) {
        if (!dni.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos");
        }
        return repository.findByDniOrderByIdAsc(dni);
    }

    @Transactional
    public Denuncia atender(int id) {
        Denuncia denuncia = obtener(id);
        denuncia.atender();
        return repository.save(denuncia);
    }
}
