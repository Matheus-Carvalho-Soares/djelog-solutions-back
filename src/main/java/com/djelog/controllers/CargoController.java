package com.djelog.controllers;


import com.djelog.dtos.CargoDTO;
import com.djelog.services.CargoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cargo")
public class CargoController {

    private CargoService cargoService;

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<CargoDTO>> findAll(){
        return cargoService.findAll().isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(cargoService.findAll());
    }
}
