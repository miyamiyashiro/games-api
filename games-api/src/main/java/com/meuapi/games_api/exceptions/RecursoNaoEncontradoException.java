package com.meuapi.games_api.exceptions;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(Long id) {
        super("Não foi possível encontrar o registro com ID: " + id);
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
