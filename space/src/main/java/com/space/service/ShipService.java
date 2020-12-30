package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface ShipService {

    Boolean isExists(Long id);

    Page<Ship> getAllShips(Specification<Ship> shipSpecification, Pageable pageable);

    void addNewShip(Ship ship) throws Exception;

    Ship findById(Long id);

    Ship updateShip(Long id, Ship ship);

    void deleteShip(Long id);

    Specification<Ship> filterShipsByName(String name);

    Specification<Ship> filterShipsByPlanet(String planet);

    Specification<Ship> filterShipsByShipType(ShipType shipType);

    Specification<Ship> filterShipsByDate(Long after, Long before);

    Specification<Ship> filterShipsByUsage(Boolean isUsed);

    Specification<Ship> filterShipsBySpeed(Double min, Double max);

    Specification<Ship> filterShipsByCrewSize(Integer min, Integer max);

    Specification<Ship> filterShipsByRating(Double min, Double max);
}