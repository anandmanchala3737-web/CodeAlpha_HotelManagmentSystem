# Hotel Room Booking Management System

A menu-driven Java console project using OOP, collections, date handling, and file I/O.

## Requirements
- JDK 17 or later
- VS Code with Java Extension Pack, IntelliJ IDEA, or Eclipse

## Run from the project folder

### Windows
```bash
javac -d out src/HotelBookingSystem.java
java -cp out HotelBookingSystem
```

### macOS / Linux
```bash
javac -d out src/HotelBookingSystem.java
java -cp out HotelBookingSystem
```

The program creates `data/hotel_data.dat` automatically. It stores rooms and bookings using Java object serialization. Keep this file to retain data between runs.

## Default room inventory

| Room numbers | Category | Price per night | Capacity |
|---|---|---:|---:|
| 101–103 | Standard | Rs. 1,500 | 2 |
| 201–202 | Deluxe | Rs. 2,500 | 3 |
| 301–302 | Suite | Rs. 4,500 | 4 |

## Features
1. View room inventory.
2. Search availability by date and category.
3. Book a room after a simulated payment.
4. View a booking using its booking ID.
5. Cancel a reservation. The simulated payment becomes REFUNDED.
6. Save data automatically to a local file.

## Important notes
- Dates must use `yyyy-mm-dd`.
- Check-out must be later than check-in.
- A room cannot be booked for overlapping confirmed reservations.
- Payment is only a simulation. No real payment is processed.
- This is an educational console project, not a production hotel payment system.

## OOP concepts demonstrated
- Encapsulation: fields are private and accessed through methods.
- Classes and objects: Room, Booking, Payment, Hotel, DataStore.
- Enums: room category, booking status, payment status.
- Composition: Booking contains a Room and Payment.
- File I/O: ObjectInputStream and ObjectOutputStream.
