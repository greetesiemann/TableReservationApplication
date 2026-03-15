package ee.siemann.laudadebroneerimine.service;

import ee.siemann.laudadebroneerimine.model.RestaurantTable;
import ee.siemann.laudadebroneerimine.repository.ReservationRepository;
import ee.siemann.laudadebroneerimine.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Finds all tables that are available for the given time and match the
     * customer's requirements for group size and preferred features.
     * @param peopleCount minimum number of seats required
     * @param features optional list of required features
     * @param time requested start time of the reservation
     * @return list of tables that fit the group, match the features, and are not already booked
     */
    public List<RestaurantTable> findAvailableTables(int peopleCount, List<String> features, LocalDateTime time) {
        List<RestaurantTable> allFree = tableRepository.findAll().stream()
                .filter(table -> table.getPlaces() >= peopleCount)
                .filter(table -> isTableFree(table, time))
                .collect(Collectors.toList());

        // try exact feature match
        if (features != null && !features.isEmpty()) {
            List<RestaurantTable> strictMatch = allFree.stream()
                    .filter(table -> table.getFeatures().containsAll(features))
                    .filter(table -> table.getPlaces() <= peopleCount + 1)
                    .collect(Collectors.toList());

            if (!strictMatch.isEmpty()) return strictMatch;

            // no exact match — ANY feature match
            List<RestaurantTable> looseMatch = allFree.stream()
                    .filter(table -> table.getFeatures().stream().anyMatch(features::contains))
                    .filter(table -> table.getPlaces() <= peopleCount + 3)
                    .collect(Collectors.toList());

            if (!looseMatch.isEmpty()) return looseMatch;
        }

        // no features selected (or all feature searches failed)
        // prefer tables with exactly peopleCount+1 seats first
        List<RestaurantTable> exactSize = allFree.stream()
                .filter(table -> table.getPlaces() <= peopleCount + 1)
                .collect(Collectors.toList());

        if (!exactSize.isEmpty()) return exactSize;

        return allFree;
    }

    /**
     * Checks whether a table has no overlapping reservations for a 2-hour slot
     * starting at the given time.
     * @param table the table to check
     * @param time the requested start time
     * @return true if the table is free for the full 2-hour duration, false if already booked
     */
    public boolean isTableFree(RestaurantTable table, LocalDateTime time) {
        LocalDateTime requestedEnd = time.plusHours(2); // Let's assume one reservation last for 2 hours
        return reservationRepository.findByRestaurantTable(table).stream()
                .noneMatch(res ->
                        time.isBefore(res.getEndTime()) &&
                                requestedEnd.isAfter(res.getStartTime())
                );
    }
}
