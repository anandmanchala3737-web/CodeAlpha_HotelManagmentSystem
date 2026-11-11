import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HotelBookingSystem {
    private static final Scanner SC = new Scanner(System.in);
    private static final String DATA_FILE = "data/hotel_data.dat";
    private static Hotel hotel;

    public static void main(String[] args) {
        hotel = DataStore.load(DATA_FILE);
        if (hotel.getRooms().isEmpty()) {
            hotel.loadDefaultRooms();
            DataStore.save(hotel, DATA_FILE);
        }

        System.out.println("\n==========================================");
        System.out.println("       HOTEL ROOM BOOKING SYSTEM");
        System.out.println("==========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--------------- MAIN MENU ---------------");
            System.out.println("1. View all rooms");
            System.out.println("2. Search available rooms");
            System.out.println("3. Book a room");
            System.out.println("4. View booking details");
            System.out.println("5. Cancel reservation");
            System.out.println("6. Exit");
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> showAllRooms();
                case 2 -> searchRooms();
                case 3 -> makeBooking();
                case 4 -> viewBooking();
                case 5 -> cancelBooking();
                case 6 -> {
                    DataStore.save(hotel, DATA_FILE);
                    System.out.println("Thank you for using the Hotel Booking System!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please select 1-6.");
            }
        }
    }

    private static void showAllRooms() {
        System.out.println("\n---------------- ROOM LIST ----------------");
        System.out.printf("%-8s %-12s %-12s %-12s%n",
                "Room No.", "Category", "Price/Night", "Capacity");
        for (Room room : hotel.getRooms()) {
            System.out.printf("%-8d %-12s Rs.%-9.2f %-12d%n",
                    room.getRoomNumber(), room.getCategory(),
                    room.getPricePerNight(), room.getCapacity());
        }
    }

    private static void searchRooms() {
        System.out.println("\n--------------- SEARCH ROOMS ---------------");
        LocalDate checkIn = readDate("Check-in date (yyyy-mm-dd): ");
        LocalDate checkOut = readDate("Check-out date (yyyy-mm-dd): ");

        if (!validDateRange(checkIn, checkOut)) return;

        RoomCategory category = readCategory(true);
        List<Room> available = hotel.searchAvailableRooms(checkIn, checkOut, category);

        if (available.isEmpty()) {
            System.out.println("No rooms are available for the selected dates/category.");
            return;
        }

        System.out.println("\nAvailable rooms:");
        for (Room room : available) {
            System.out.printf("Room %d | %-8s | Rs.%.2f per night | Capacity: %d%n",
                    room.getRoomNumber(), room.getCategory(),
                    room.getPricePerNight(), room.getCapacity());
        }
    }

    private static void makeBooking() {
        System.out.println("\n---------------- ROOM BOOKING ----------------");
        String guestName = readNonEmpty("Guest name: ");
        String phone = readNonEmpty("Phone number: ");
        LocalDate checkIn = readDate("Check-in date (yyyy-mm-dd): ");
        LocalDate checkOut = readDate("Check-out date (yyyy-mm-dd): ");

        if (!validDateRange(checkIn, checkOut)) return;

        RoomCategory category = readCategory(false);
        List<Room> available = hotel.searchAvailableRooms(checkIn, checkOut, category);

        if (available.isEmpty()) {
            System.out.println("Sorry, no rooms are available in this category.");
            return;
        }

        System.out.println("\nAvailable rooms:");
        for (Room room : available) {
            System.out.printf("Room %d | Rs.%.2f per night | Capacity: %d%n",
                    room.getRoomNumber(), room.getPricePerNight(), room.getCapacity());
        }

        int roomNumber = readInt("Enter the room number you want to book: ");
        Room selectedRoom = hotel.findRoom(roomNumber);

        if (selectedRoom == null || !available.contains(selectedRoom)) {
            System.out.println("Invalid room number or room is no longer available.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double amount = nights * selectedRoom.getPricePerNight();

        System.out.println("\n--------------- BOOKING SUMMARY ---------------");
        System.out.println("Guest       : " + guestName);
        System.out.println("Room        : " + selectedRoom.getRoomNumber());
        System.out.println("Category    : " + selectedRoom.getCategory());
        System.out.println("Check-in    : " + checkIn);
        System.out.println("Check-out   : " + checkOut);
        System.out.println("Nights      : " + nights);
        System.out.printf("Total amount: Rs.%.2f%n", amount);

        String confirm = readNonEmpty("Continue to payment? (Y/N): ");
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Booking process stopped.");
            return;
        }

        Payment payment = Payment.simulate(amount, SC);
        if (!payment.isSuccessful()) {
            System.out.println("Payment failed. No reservation was created.");
            return;
        }

        Booking booking = hotel.createBooking(
                guestName, phone, selectedRoom, checkIn, checkOut, payment);

        DataStore.save(hotel, DATA_FILE);
        System.out.println("\n*** BOOKING CONFIRMED SUCCESSFULLY! ***");
        printBooking(booking);
    }

    private static void viewBooking() {
        System.out.println("\n--------------- BOOKING DETAILS ---------------");
        String id = readNonEmpty("Enter booking ID: ");
        Booking booking = hotel.findBooking(id);

        if (booking == null) {
            System.out.println("Booking not found.");
        } else {
            printBooking(booking);
        }
    }

    private static void cancelBooking() {
        System.out.println("\n--------------- CANCEL RESERVATION ---------------");
        String id = readNonEmpty("Enter booking ID: ");
        Booking booking = hotel.findBooking(id);

        if (booking == null) {
            System.out.println("Booking not found.");
            return;
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println("This booking is already cancelled.");
            return;
        }

        printBooking(booking);
        String confirm = readNonEmpty("Are you sure you want to cancel? (Y/N): ");
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Cancellation stopped.");
            return;
        }

        booking.cancel();
        DataStore.save(hotel, DATA_FILE);
        System.out.println("Reservation cancelled successfully.");
        System.out.println("Payment status: " + booking.getPayment().getStatus());
        System.out.println("Note: This is a simulation; no real money was charged or refunded.");
    }

    private static void printBooking(Booking b) {
        System.out.println("\n--------------- BOOKING RECEIPT ---------------");
        System.out.println("Booking ID  : " + b.getBookingId());
        System.out.println("Guest       : " + b.getGuestName());
        System.out.println("Phone       : " + b.getPhone());
        System.out.println("Room        : " + b.getRoom().getRoomNumber());
        System.out.println("Category    : " + b.getRoom().getCategory());
        System.out.println("Check-in    : " + b.getCheckIn());
        System.out.println("Check-out   : " + b.getCheckOut());
        System.out.println("Nights      : " + b.getNumberOfNights());
        System.out.printf("Total       : Rs.%.2f%n", b.getTotalAmount());
        System.out.println("Booking     : " + b.getStatus());
        System.out.println("Payment     : " + b.getPayment().getStatus());
        System.out.println("Booked on   : " + b.getBookedOn());
        System.out.println("-----------------------------------------------");
    }

    private static boolean validDateRange(LocalDate in, LocalDate out) {
        if (in.isBefore(LocalDate.now())) {
            System.out.println("Check-in date cannot be in the past.");
            return false;
        }
        if (!out.isAfter(in)) {
            System.out.println("Check-out must be after check-in.");
            return false;
        }
        return true;
    }

    private static RoomCategory readCategory(boolean allowAll) {
        while (true) {
            System.out.println("Room category:");
            System.out.println("1. Standard");
            System.out.println("2. Deluxe");
            System.out.println("3. Suite");
            if (allowAll) System.out.println("4. All categories");
            int choice = readInt("Choose category: ");
            switch (choice) {
                case 1: return RoomCategory.STANDARD;
                case 2: return RoomCategory.DELUXE;
                case 3: return RoomCategory.SUITE;
                case 4:
                    if (allowAll) return null;
                    break;
                default:
                    break;
            }
            System.out.println("Invalid category. Try again.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(SC.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(SC.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid date. Use yyyy-mm-dd, for example 2026-12-20.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SC.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }
}

enum RoomCategory {
    STANDARD, DELUXE, SUITE
}

enum BookingStatus {
    CONFIRMED, CANCELLED
}

enum PaymentStatus {
    SUCCESS, FAILED, REFUNDED
}

class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int roomNumber;
    private final RoomCategory category;
    private final double pricePerNight;
    private final int capacity;

    public Room(int roomNumber, RoomCategory category, double pricePerNight, int capacity) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomCategory getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }
}

class Payment implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String transactionId;
    private final double amount;
    private PaymentStatus status;

    private Payment(double amount, PaymentStatus status) {
        this.transactionId = "TXN-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        this.amount = amount;
        this.status = status;
    }

    public static Payment simulate(double amount, Scanner input) {
        System.out.println("\n--------------- PAYMENT SIMULATION ---------------");
        System.out.printf("Amount to pay: Rs.%.2f%n", amount);
        System.out.println("1. Simulate successful payment");
        System.out.println("2. Simulate failed payment");
        while (true) {
            System.out.print("Select payment result: ");
            String choice = input.nextLine().trim();
            if (choice.equals("1")) {
                Payment payment = new Payment(amount, PaymentStatus.SUCCESS);
                System.out.println("Payment successful. Transaction: " + payment.transactionId);
                return payment;
            } else if (choice.equals("2")) {
                Payment payment = new Payment(amount, PaymentStatus.FAILED);
                System.out.println("Payment failed.");
                return payment;
            }
            System.out.println("Choose 1 or 2.");
        }
    }

    public void refund() {
        if (status == PaymentStatus.SUCCESS) status = PaymentStatus.REFUNDED;
    }

    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public boolean isSuccessful() { return status == PaymentStatus.SUCCESS; }
}

class Booking implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bookingId;
    private final String guestName;
    private final String phone;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final LocalDate bookedOn;
    private final Payment payment;
    private BookingStatus status;

    public Booking(String guestName, String phone, Room room,
                   LocalDate checkIn, LocalDate checkOut, Payment payment) {
        this.bookingId = "BK-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        this.guestName = guestName;
        this.phone = phone;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.payment = payment;
        this.bookedOn = LocalDate.now();
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == BookingStatus.CONFIRMED) {
            status = BookingStatus.CANCELLED;
            payment.refund();
        }
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public double getTotalAmount() {
        return getNumberOfNights() * room.getPricePerNight();
    }

    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public String getPhone() { return phone; }
    public Room getRoom() { return room; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public LocalDate getBookedOn() { return bookedOn; }
    public Payment getPayment() { return payment; }
    public BookingStatus getStatus() { return status; }
}

class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Room> rooms = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public void loadDefaultRooms() {
        rooms.clear();
        rooms.add(new Room(101, RoomCategory.STANDARD, 1500, 2));
        rooms.add(new Room(102, RoomCategory.STANDARD, 1500, 2));
        rooms.add(new Room(103, RoomCategory.STANDARD, 1500, 2));
        rooms.add(new Room(201, RoomCategory.DELUXE, 2500, 3));
        rooms.add(new Room(202, RoomCategory.DELUXE, 2500, 3));
        rooms.add(new Room(301, RoomCategory.SUITE, 4500, 4));
        rooms.add(new Room(302, RoomCategory.SUITE, 4500, 4));
    }

    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut,
                                           RoomCategory category) {
        List<Room> available = new ArrayList<>();
        for (Room room : rooms) {
            if (category != null && room.getCategory() != category) continue;
            boolean occupied = false;
            for (Booking booking : bookings) {
                if (booking.getRoom().getRoomNumber() == room.getRoomNumber()
                        && booking.getStatus() == BookingStatus.CONFIRMED
                        && checkIn.isBefore(booking.getCheckOut())
                        && checkOut.isAfter(booking.getCheckIn())) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) available.add(room);
        }
        return available;
    }

    public Booking createBooking(String name, String phone, Room room,
                                 LocalDate in, LocalDate out, Payment payment) {
        Booking booking = new Booking(name, phone, room, in, out, payment);
        bookings.add(booking);
        return booking;
    }

    public Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) return room;
        }
        return null;
    }

    public Booking findBooking(String id) {
        for (Booking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(id)) return booking;
        }
        return null;
    }

    public List<Room> getRooms() { return rooms; }
    public List<Booking> getBookings() { return bookings; }
}

class DataStore {
    public static Hotel load(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) return new Hotel();

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object object = in.readObject();
            if (object instanceof Hotel) return (Hotel) object;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not read saved data. Starting with a new hotel.");
        }
        return new Hotel();
    }

    public static void save(Hotel hotel, String fileName) {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(hotel);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
}
