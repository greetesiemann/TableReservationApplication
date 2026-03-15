package ee.siemann.laudadebroneerimine.service;

import ee.siemann.laudadebroneerimine.model.Reservation;
import ee.siemann.laudadebroneerimine.model.RestaurantTable;
import ee.siemann.laudadebroneerimine.repository.ReservationRepository;
import ee.siemann.laudadebroneerimine.repository.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final Random random = new Random();
    private final ReservationService reservationService;

    public DataInitializer(TableRepository tableRepository, ReservationRepository reservationRepository, ReservationService reservationService) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tableRepository.count() == 0) {
            tableRepository.save(new RestaurantTable(null, 1, 2, List.of("Akna all", "Vaikne ala"), false));
            tableRepository.save(new RestaurantTable(null, 2, 4, List.of("Akna all"), false));
            tableRepository.save(new RestaurantTable(null, 3, 2, List.of("Vaatega avatud köögile"), false));
            tableRepository.save(new RestaurantTable(null, 4, 6, List.of("Vaikne ala"), false));
            tableRepository.save(new RestaurantTable(null, 5, 8, List.of("Akna all", "Vaatega avatud köögile"), false));
            tableRepository.save(new RestaurantTable(null, 6, 4, List.of("Vaikne ala", "Vaatega avatud köögile"), false));
            tableRepository.save(new RestaurantTable(null, 7, 2, List.of(), false));
            tableRepository.save(new RestaurantTable(null, 8, 8, List.of("Terass"), false));
            tableRepository.save(new RestaurantTable(null, 9, 4, List.of("Terass"), false));
            tableRepository.save(new RestaurantTable(null, 10, 2, List.of("Terass"), false));
            tableRepository.save(new RestaurantTable(null, 11, 4, List.of("Terass"), false));
            System.out.println("Tables are added");
        }
        List<RestaurantTable> tables = tableRepository.findAll();
        for (RestaurantTable table : tables) {
            int reservationCount = 1 + random.nextInt(3); // 1 to 3 reservations per table
            for (int i = 0; i < reservationCount; i++) {
                LocalDate randomDate = LocalDate.now().plusDays(random.nextInt(7)); // next 7 days
                LocalDateTime startTime = randomDate.atTime(11 + random.nextInt(10), 0); // 11:00–21:00

                if (reservationService.isTableFree(table, startTime)) {
                    Reservation res = Reservation.builder()
                            .restaurantTable(table)
                            .startTime(startTime)
                            .endTime(startTime.plusHours(2))
                            .personCount(random.nextInt(table.getPlaces()) + 1)
                            .customerName("Random client")
                            .build();

                    reservationRepository.save(res);
                }

            }
        }

    }
}
