package com.danh.bookingtour.controller;

import com.danh.bookingtour.dto.ClientRequest;
import com.danh.bookingtour.dto.ClientResponse;
import com.danh.bookingtour.entity.Client;
import com.danh.bookingtour.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientMapper clientMapper;

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll(@RequestParam(required = false) String type) {
        List<Client> clients = clientMapper.findAllActive(type);
        return ResponseEntity.ok(clients.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        return clientMapper.findByIdActive(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ClientRequest request) {
        if (clientMapper.existsByNameAndType(request.getName(), request.getType().name(), null)) {
            return ResponseEntity.badRequest().body("Client with this name and type already exists");
        }

        Client client = Client.builder()
                .name(request.getName())
                .type(request.getType())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .description(request.getDescription())
                .isActive(true)
                .build();

        clientMapper.insert(client);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ClientRequest request) {
        if (clientMapper.existsByNameAndType(request.getName(), request.getType().name(), id)) {
            return ResponseEntity.badRequest().body("Another client with this name and type already exists");
        }

        return clientMapper.findByIdActive(id).map(existing -> {
            existing.setName(request.getName());
            existing.setType(request.getType());
            existing.setContactName(request.getContactName());
            existing.setPhone(request.getPhone());
            existing.setEmail(request.getEmail());
            existing.setAddress(request.getAddress());
            existing.setDescription(request.getDescription());
            
            clientMapper.update(existing);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientMapper.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .type(client.getType())
                .contactName(client.getContactName())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                .description(client.getDescription())
                .isActive(client.getIsActive())
                .createdAt(client.getCreatedAt())
                .build();
    }
}
