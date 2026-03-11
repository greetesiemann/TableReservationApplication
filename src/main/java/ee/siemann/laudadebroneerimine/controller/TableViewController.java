package ee.siemann.laudadebroneerimine.controller;

import ee.siemann.laudadebroneerimine.model.Reservation;
import ee.siemann.laudadebroneerimine.model.RestaurantTable;
import ee.siemann.laudadebroneerimine.repository.ReservationRepository;
import ee.siemann.laudadebroneerimine.repository.TableRepository;
import ee.siemann.laudadebroneerimine.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class TableViewController {

    private final TableRepository tableRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    public TableViewController(TableRepository tableRepository, ReservationService reservationService, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    // 2. /search — now returns to index (not layout)
    @PostMapping("/search")
    public String searchTables(
            @RequestParam int peopleCount,
            @RequestParam(required = false) List<String> features,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            Model model) {

        LocalDateTime requestedDateTime = LocalDateTime.of(date, time);

        List<RestaurantTable> availableTables =
                reservationService.findAvailableTables(peopleCount, features, requestedDateTime);

        List<RestaurantTable> allTables = tableRepository.findAll();

        // collect IDs of available tables so the template can grey out the rest
        Set<Long> availableIds = availableTables.stream()
                .map(RestaurantTable::getId)
                .collect(Collectors.toSet());

        RestaurantTable bestTable = null;
        int minExtraSeats = Integer.MAX_VALUE;

        for (RestaurantTable table : allTables) {
            table.setRecommended(false);
            if (availableIds.contains(table.getId())) {
                int extra = table.getPlaces() - peopleCount;
                if (extra < minExtraSeats) {
                    minExtraSeats = extra;
                    bestTable = table;
                }
            }
        }

        if (bestTable != null) bestTable.setRecommended(true);

        model.addAttribute("tables", allTables);
        model.addAttribute("availableIds", availableIds);   // ← new
        model.addAttribute("peopleCount", peopleCount);
        model.addAttribute("bookingDate", date);
        model.addAttribute("bookingTime", time);
        model.addAttribute("selectedFeatures", features);   // ← keeps checkboxes ticked

        return "index";   // ← same page, not "layout"
    }

    // 3. /book/{id} — now saves the reservation
    @GetMapping("/book/{id}")
    public String confirmBooking(
            @PathVariable Long id,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int peopleCount,
            @RequestParam String customerName,
            Model model) {

        RestaurantTable table = tableRepository.findById(id).orElseThrow();

        LocalDateTime start = LocalDateTime.of(
                LocalDate.parse(date),
                LocalTime.parse(time)
        );

        Reservation reservation = Reservation.builder()
                .restaurantTable(table)
                .startTime(start)
                .endTime(start.plusHours(2))
                .personCount(peopleCount)
                .customerName(customerName)
                .build();

        reservationRepository.save(reservation);

        model.addAttribute("table", table);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("customerName", customerName);

        return "thanks";
    }
}
