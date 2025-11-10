package com.motorplus.motorplus.services;

import com.motorplus.motorplus.dto.ordersDtos.OrderDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleCreateDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleFilter;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleHistoryDto;
import com.motorplus.motorplus.dto.vehicleDtos.VehicleUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicioVehiculo {
    Page<VehicleDto> list(VehicleFilter f, Pageable p);
    VehicleDto get(String placa);
    VehicleDto create(VehicleCreateDto dto);
    VehicleDto update(String placa, VehicleUpdateDto dto);
    void delete(String placa);
    Page<OrderDto> listOrders(String placa, Pageable p);
    VehicleHistoryDto history(String placa);
}
