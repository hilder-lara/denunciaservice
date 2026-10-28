package com.lara.denunciaservice.exception;

public class DenunciaNoEncontradaException extends RuntimeException {
    public DenunciaNoEncontradaException(int id) {
        super("No existe la denuncia con código " + id);
    }
}
