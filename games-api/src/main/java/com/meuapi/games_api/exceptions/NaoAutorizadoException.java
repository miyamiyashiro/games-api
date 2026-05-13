package com.meuapi.games_api.exceptions;

public class NaoAutorizadoException extends RuntimeException {
    public NaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
