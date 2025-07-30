package com.bookingbarber.sys.javafx;

import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;

public final class UserSession {

    private static UserSession instance;
    private ClienteResponseDTO cliente;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public ClienteResponseDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteResponseDTO cliente) {
        this.cliente = cliente;
    }

    public void cleanUserSession() {
        cliente = null;
    }
}
