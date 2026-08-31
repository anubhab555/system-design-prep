package com.systemdesign.ecommerce_and_booking_system.movie_booking_system;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.City;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Cinema;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Movie;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.User;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.payment.PaymentStrategy;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.strategies.pricing.PricingStrategy;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Show;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Screen;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Seat;
import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.model.Booking;

import com.systemdesign.ecommerce_and_booking_system.movie_booking_system.enums.SeatType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;


class MovieBookingService {
    private static volatile MovieBookingService instance;

    private final Map<String, City> cities;
    private final Map<String, Cinema> cinemas;
    private final Map<String, Movie> movies;
    private final Map<String, User> users;
    private final Map<String, Show> shows;
    private final Map<Screen, Cinema> screenToCinema;

    // Core services - managed by the system
    private final SeatLockManager seatLockManager;
    private final BookingManager bookingManager;


    private MovieBookingService() {
        this.cities = new ConcurrentHashMap<>();
        this.cinemas = new ConcurrentHashMap<>();
        this.movies = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.shows = new ConcurrentHashMap<>();
        this.screenToCinema = new ConcurrentHashMap<>();

        this.seatLockManager = new SeatLockManager();
        this.bookingManager = new BookingManager(seatLockManager);
    }

    public static MovieBookingService getInstance() {
        if (instance == null) {
            synchronized (MovieBookingService.class) {
                if (instance == null) {
                    instance = new MovieBookingService();
                }
            }
        }
        return instance;
    }

    // --- Data Management Methods ---
    public City addCity(String id, String name) {
        City city = new City(id, name);
        cities.put(city.getId(), city);
        return city;
    }

    public Cinema addCinema(String id, String name, String cityId, List<Screen> screens) {
        City city = cities.get(cityId);
        Cinema cinema = new Cinema(id, name, city, screens);
        cinemas.put(cinema.getId(), cinema);
        for (Screen screen : screens) {
            screenToCinema.put(screen, cinema);
        }
        return cinema;
    }

    public void addMovie(Movie movie) {
        this.movies.put(movie.getId(), movie);
    }

    public Show addShow(String id, Movie movie, Screen screen, LocalDateTime startTime, Map<SeatType, Double> seatPrices, PricingStrategy pricingStrategy) {
        Show show = new Show(id, movie, screen, startTime, seatPrices, pricingStrategy);
        shows.put(show.getId(), show);
        return show;
    }

    public User createUser(String name, String email) {
        User user = new User(name, email);
        users.put(user.getUserId(), user);
        return user;
    }

    public Optional<Booking> bookTickets(String userId, String showId, List<Seat> desiredSeats, PaymentStrategy paymentStrategy) {
        return bookingManager.createBooking(
                users.get(userId),
                shows.get(showId),
                desiredSeats,
                paymentStrategy
        );
    }

    // --- Search Functionality ---
    public List<Show> findShows(String movieTitle, String cityName) {
        List<Show> result = new ArrayList<>();
        shows.values().stream()
            .filter(show -> show.getMovie().getTitle().equalsIgnoreCase(movieTitle))
            .filter(show -> {
                Cinema cinema = findCinemaForShow(show);
                return cinema != null && cinema.getCity().getName().equalsIgnoreCase(cityName);
            })
            .forEach(result::add);
        return result;
    }

    private Cinema findCinemaForShow(Show show) {
        // O(1) lookup: the screen-to-cinema index is built as cinemas are added.
        return screenToCinema.get(show.getScreen());
    }

    public void shutdown() {
        this.seatLockManager.shutdown();
        System.out.println("MovieTicketBookingSystem has been shut down.");
    }
}
