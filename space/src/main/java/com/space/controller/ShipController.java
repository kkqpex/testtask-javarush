package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;


@SuppressWarnings("Duplicates")
@RestController
@RequestMapping(value = "/rest")
public class ShipController {

    private static Logger log = getLogger(ShipController.class);

    @Resource(name = "shipServiceImpl")
    private ShipService shipService;

    @GetMapping(value = "/ships")
    public ResponseEntity<List<Ship>> showAllShips(
            @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating) {

        log.info("Running method 'showAllShips()'...");

        log.info("Getting pageable and sort options...");
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        log.info("Getting list of all ships...");
        List<Ship> shipsList = shipService.getAllShips(
                Specification.where(shipService.filterShipsByName(name)
                        .and(shipService.filterShipsByPlanet(planet)))
                        .and(shipService.filterShipsByShipType(shipType))
                        .and(shipService.filterShipsByDate(after, before))
                        .and(shipService.filterShipsByUsage(isUsed))
                        .and(shipService.filterShipsBySpeed(minSpeed, maxSpeed))
                        .and(shipService.filterShipsByCrewSize(minCrewSize, maxCrewSize))
                        .and(shipService.filterShipsByRating(minRating, maxRating)), pageable).getContent();

        if (shipsList.isEmpty()) {
            log.debug("List of ships is empty...");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(shipsList, HttpStatus.OK);
    }

    @GetMapping(value = "/ships/count")
    public ResponseEntity<Integer> showShipsCount(@RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "planet", required = false) String planet,
                                                  @RequestParam(value = "shipType", required = false) ShipType shipType,
                                                  @RequestParam(value = "after", required = false) Long after,
                                                  @RequestParam(value = "before", required = false) Long before,
                                                  @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                                  @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                                  @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                                  @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                                  @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                                  @RequestParam(value = "minRating", required = false) Double minRating,
                                                  @RequestParam(value = "maxRating", required = false) Double maxRating) {

        log.info("Running method 'showShipsCount()'...");

        log.info("Getting count for list of all ships...");
        Integer shipsCount = shipService.getAllShips(
                Specification.where(shipService.filterShipsByName(name)
                        .and(shipService.filterShipsByPlanet(planet)))
                        .and(shipService.filterShipsByShipType(shipType))
                        .and(shipService.filterShipsByDate(after, before))
                        .and(shipService.filterShipsByUsage(isUsed))
                        .and(shipService.filterShipsBySpeed(minSpeed, maxSpeed))
                        .and(shipService.filterShipsByCrewSize(minCrewSize, maxCrewSize))
                        .and(shipService.filterShipsByRating(minRating, maxRating)),
                Pageable.unpaged()).getNumberOfElements();

        return new ResponseEntity<>(shipsCount, HttpStatus.OK);
    }


    @PostMapping(value = "/ships")
    public ResponseEntity<Ship> createNewShip(@RequestBody Ship ship) {

        log.info("Running method 'createNewShip()'...");

        try {
            log.info("Try add new ship to database...");
            shipService.addNewShip(ship);
        } catch (Exception e) {
            log.debug("Incorrect data to adding new Ship...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        log.info("New ship is added: " + ship.toString());
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @GetMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> getShipById(@PathVariable("id") Long id) {

        log.info("Running method 'getShipById()'...");

        log.info("Getting ship by id: " + id + "...");
        Ship ship;
        if (id <= 0) {
            log.debug("Incorrect id. Id must be > 0...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!shipService.isExists(id)) {
            log.debug(id + " - is not exists...");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            ship = shipService.findById(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("Ship is found: " + ship.toString());
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long id, @RequestBody Ship ship) {

        log.info("Running method 'updateShip()'...");

        if (id <= 0) {
            log.debug("Incorrect id - " + id + ". Id must be > 0...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!shipService.isExists(id)) {
            log.debug(id + " - is not exists...");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            log.info("Try update ship in database...");
            return new ResponseEntity<>(shipService.updateShip(id, ship), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long id) {

        log.info("Running method 'deleteShip()'...");

        if (id <= 0) {
            log.debug("Incorrect id - " + id + ". Id must be > 0...");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!shipService.isExists(id)) {
            log.debug(id + " - is not exists...");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            log.info("Try delete ship...");
            shipService.deleteShip(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("Ship with id: " + id + " is delete...");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}