package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
public class ShipServiceImpl implements ShipService {

    @Resource(name = "shipRepository")
    private ShipRepository shipRepository;

    @Override
    public Boolean isExists(Long id) {
        return shipRepository.existsById(id);
    }

    @Override
    public Page<Ship> getAllShips(Specification<Ship> shipSpecification, Pageable pageable) {
        return shipRepository.findAll(shipSpecification, pageable);
    }

    @Override
    public void addNewShip(Ship ship) {

        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        validateProdDate(ship.getProdDate());

        Double rating = calculateRating(ship);
        ship.setRating(rating);

        shipRepository.save(ship);
    }

    @Override
    public Ship findById(Long id) {
        if (shipRepository.findById(id).isPresent()) {
            return shipRepository.findById(id).get();
        }
        return null;
    }

    @Override
    public Ship updateShip(Long id, Ship ship) {

        Ship updateShip = new Ship();

        if (shipRepository.findById(id).isPresent()) {
            updateShip = shipRepository.findById(id).get();
        }

        if (ship.getName() != null) {
            updateShip.setName(ship.getName());
        }

        if (ship.getPlanet() != null) {
            updateShip.setPlanet(ship.getPlanet());
        }

        if (ship.getShipType() != null) {
            updateShip.setShipType(ship.getShipType());
        }

        if (ship.getProdDate() != null) {
            updateShip.setProdDate(validateProdDate(ship.getProdDate()));
        }

        if (ship.getUsed() != null) {
            updateShip.setUsed(ship.getUsed());
        }

        if (ship.getSpeed() != null) {
            updateShip.setSpeed(ship.getSpeed());
        }

        if (ship.getCrewSize() != null) {
            updateShip.setCrewSize(ship.getCrewSize());
        }

        Double newRating = calculateRating(updateShip);
        updateShip.setRating(newRating);

        return shipRepository.save(updateShip);
    }

    @Override
    public void deleteShip(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public Specification<Ship> filterShipsByName(String name) {
        return (Specification<Ship>) (root, query, criteriaBuilder) ->
                name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Ship> filterShipsByPlanet(String planet) {

        return (Specification<Ship>) (root, query, criteriaBuilder) ->
                planet == null ? null : criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    @Override
    public Specification<Ship> filterShipsByShipType(ShipType shipType) {

        return (Specification<Ship>) (root, query, criteriaBuilder) ->
                shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    @Override
    public Specification<Ship> filterShipsByDate(Long after, Long before) {
        return (Specification<Ship>) (root, query, criteriaBuilder) ->
        {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date beforeDate = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), beforeDate);
            }
            if (before == null) {
                Date afterDate = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), afterDate);
            }
            Date beforeDate = new Date(before);
            Date afterDate = new Date(after);
            return criteriaBuilder.between(root.get("prodDate"), afterDate, beforeDate);
        };
    }

    @Override
    public Specification<Ship> filterShipsByUsage(Boolean isUsed) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (isUsed == null) {
                return null;
            }
            if (isUsed) {
                return criteriaBuilder.isTrue(root.get("isUsed"));
            }
            return criteriaBuilder.isFalse(root.get("isUsed"));
        };
    }

    @Override
    public Specification<Ship> filterShipsBySpeed(Double min, Double max) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), min);
            }
            return criteriaBuilder.between(root.get("speed"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterShipsByCrewSize(Integer min, Integer max) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), min);
            }
            return criteriaBuilder.between(root.get("crewSize"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterShipsByRating(Double min, Double max) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), min);
            }
            return criteriaBuilder.between(root.get("rating"), min, max);
        };
    }

    private Date validateProdDate(Date date) {
        Calendar dateMinCal = Calendar.getInstance();
        Calendar dateMaxCal = Calendar.getInstance();

        dateMinCal.set(2800, Calendar.JANUARY, 1);
        dateMaxCal.set(3019, Calendar.DECEMBER, 31);

        if (date.getTime() < 0 || date.getTime() < dateMinCal.getTimeInMillis()
                || date.getTime() > dateMaxCal.getTimeInMillis()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Date...");
        } else {
            return date;
        }
    }

    private Double calculateRating(Ship ship) {

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(ship.getProdDate());

        int year = calendar.get(Calendar.YEAR);
        double k = ship.getUsed() ? 0.5 : 1.0;

        double currentRating = ((80 * ship.getSpeed() * k) / (3019 - year + 1));
        currentRating = Math.round(currentRating * 100.0) / 100.0;
        return currentRating;
    }
}