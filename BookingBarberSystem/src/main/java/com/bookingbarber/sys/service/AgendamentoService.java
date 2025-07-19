package com.bookingbarber.sys.service;

import com.bookingbarber.sys.entities.Agendamento;
import com.bookingbarber.sys.repositorys.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Transactional
    public Agendamento insert(Agendamento agendamento){

    }
}
