package com.motorplus.motorplus.controller;

import com.motorplus.motorplus.dto.ordersDtos.OrderDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleFilter;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleUpdateDto;
import com.motorplus.motorplus.services.ServicioVehiculo;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:3000"})
public class VehicleController {

    private final ServicioVehiculo servicioVehiculo;

    public VehicleController(ServicioVehiculo servicioVehiculo) {
        this.servicioVehiculo = servicioVehiculo;
    }

    @GetMapping
    public Page<VehicleDto> list(@ModelAttribute VehicleFilter filter, @PageableDefault(size = 20) Pageable pageable) {
        return servicioVehiculo.list(filter, pageable);
    }

    @GetMapping("/{plate}")
    public VehicleDto get(@PathVariable("plate") String plate) {
        return servicioVehiculo.get(plate);
    }

    @PostMapping
    public ResponseEntity<VehicleDto> create(@Valid @RequestBody VehicleCreateDto dto) {
        VehicleDto created = servicioVehiculo.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{plate}")
    public VehicleDto update(@PathVariable("plate") String plate, @Valid @RequestBody VehicleUpdateDto dto) {
        return servicioVehiculo.update(plate, dto);
    }

    @DeleteMapping("/{plate}")
    public ResponseEntity<Void> delete(@PathVariable("plate") String plate) {
        servicioVehiculo.delete(plate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{plate}/orders")
    public Page<OrderDto> listOrders(@PathVariable("plate") String plate, @PageableDefault(size = 20) Pageable pageable) {
        return servicioVehiculo.listOrders(plate, pageable);
    }

    @GetMapping("/{plate}/history")
    public VehicleHistoryDto history(@PathVariable("plate") String plate) {
        return servicioVehiculo.history(plate);
    }
}
