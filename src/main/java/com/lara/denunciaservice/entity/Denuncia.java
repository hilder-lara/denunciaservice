package com.lara.denunciaservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
public class Denuncia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int estado = 1;

    protected Denuncia() {
        // Constructor requerido por JPA.
    }

    public Denuncia(String dni, LocalDateTime fecha, String tipo, String direccion, String descripcion) {
        this.dni = dni;
        this.fecha = fecha;
        this.tipo = tipo;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.estado = 1;
    }

    public void atender() {
        this.estado = 0;
    }

    public Integer getId() { return id; }
    public String getDni() { return dni; }
    public LocalDateTime getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getDireccion() { return direccion; }
    public String getDescripcion() { return descripcion; }
    public int getEstado() { return estado; }
}
