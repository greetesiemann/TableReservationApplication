package ee.siemann.laudadebroneerimine.repository;

import ee.siemann.laudadebroneerimine.model.Reservation;
import ee.siemann.laudadebroneerimine.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRestaurantTable(RestaurantTable restaurantTable);
}
