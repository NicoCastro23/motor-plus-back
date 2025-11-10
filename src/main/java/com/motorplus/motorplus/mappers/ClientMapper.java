package com.motorplus.motorplus.mappers;

import com.motorplus.motorplus.domain.client.Client;
import com.motorplus.motorplus.dto.usersDtos.ClientCreateDto;
import com.motorplus.motorplus.dto.usersDtos.ClientDto;
import com.motorplus.motorplus.dto.usersDtos.ClientUpdateDto;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientDto toDto(Client client) {
        return new ClientDto(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCreatedAt()
        );
    }

    public Client toEntity(ClientCreateDto dto) {
        Client client = new Client();
        client.setFirstName(dto.firstName());
        client.setLastName(dto.lastName());
        client.setEmail(dto.email());
        client.setPhone(dto.phone());
        return client;
    }

    public void updateEntity(ClientUpdateDto dto, Client client) {
        client.setFirstName(dto.firstName());
        client.setLastName(dto.lastName());
        client.setEmail(dto.email());
        client.setPhone(dto.phone());
    }
}
