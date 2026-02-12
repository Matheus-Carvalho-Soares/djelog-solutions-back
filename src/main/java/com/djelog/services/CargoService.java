package com.djelog.services;


import com.djelog.dtos.CargoDTO;
import com.djelog.entities.Cargo;
import com.djelog.repositories.CargoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargoService {

    private CargoRepository cargoRepository;

    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    public List<CargoDTO> findAll(){
        return entityToDto(cargoRepository.findAll());
    }


    public List<CargoDTO> entityToDto(List<Cargo> cargos){
        return cargos.stream()
                .map(cargo -> {
                    CargoDTO dto = new CargoDTO();
                    dto.setId(cargo.getId());
                    dto.setNome(cargo.getNome());
                    dto.setObservacao(cargo.getObservacao());
                    return dto;
                }).collect(Collectors.toList());
    }
}
