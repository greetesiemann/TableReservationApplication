package ee.siemann.laudadebroneerimine.controller;

import ee.siemann.laudadebroneerimine.model.RestaurantTable;
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

@Controller
public class TableViewController {

    private final TableRepository tableRepository;
    private ReservationService reservationService;

    public TableViewController(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    // AKEN 1: Tervitusleht (avaneb aadressil localhost:8080)
    @GetMapping("/")
    public String showWelcomePage() {
        return "index"; // See avab resources/templates/index.html
    }

    // AKEN 2: Saaliplaan (avaneb pärast "Leia laud" vajutamist)
    @PostMapping("/search")
    public String searchTables(
            @RequestParam int peopleCount,
            @RequestParam(required = false) List<String> features,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, // Täpne formaat
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,     // Täpne formaat
            Model model) {

        LocalDateTime requestedDateTime = LocalDateTime.of(date, time);

        // KASUTAME TEENUST: Saame ainult vabad ja sobivad lauad
        List<RestaurantTable> availableTables = reservationService.findAvailableTables(peopleCount, features, requestedDateTime);

        // NB! Et saaliplaan töötaks, peame ikka näitama KÕIKI laudu,
        // aga märgime kuldseks (isRecommended) ainult parima vaba laua.
        List<RestaurantTable> allTables = tableRepository.findAll();

        RestaurantTable bestTable = null;
        int minExtraSeats = Integer.MAX_VALUE;

        for (RestaurantTable table : allTables) {
            table.setRecommended(false);

            // Kontrollime, kas see laud on meie "vabade" listis
            if (availableTables.contains(table)) {
                int extraSeats = table.getPlaces() - peopleCount;
                if (extraSeats < minExtraSeats) {
                    minExtraSeats = extraSeats;
                    bestTable = table;
                }
            }
        }

        // Märgime ainult ühe parima laua kuldseks
        if (bestTable != null) {
            bestTable.setRecommended(true);
        }

        model.addAttribute("tables", allTables);
        model.addAttribute("peopleCount", peopleCount);
        model.addAttribute("bookingDate", date);
        model.addAttribute("bookingTime", time);

        return "layout";
    }

    // AKEN 3: Tänukiri
    @GetMapping("/book/{id}")
    public String confirmBooking(
            @PathVariable Long id,
            @RequestParam String date,
            @RequestParam String time,
            Model model) {

        RestaurantTable table = tableRepository.findById(id).orElseThrow();
        model.addAttribute("table", table);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        return "thanks";
    }
}
