package com.lara.denunciaservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// El cliente no controla el identificador ni el estado inicial.
public record CrearDenunciaRequest(
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "[0-9]{8}", message = "El DNI debe contener exactamente 8 dígitos")
        String dni,

        @NotNull(message = "La fecha es obligatoria")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fecha,

        @NotBlank(message = "El tipo es obligatorio")
        @Size(max = 30, message = "El tipo admite hasta 30 caracteres")
        String tipo,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección admite hasta 200 caracteres")
        String direccion,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción admite hasta 255 caracteres")
        String descripcion
) {}
