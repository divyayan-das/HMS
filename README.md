# Hotel Management System

A JavaFX desktop application for hotel front-desk operations; rooms, customers, bookings, and billing — built with a clean MVC architecture.

## Features

- Room inventory management (types, rates, availability)
- Customer records and booking workflows (check-in / check-out)
- Billing generation tied to bookings
- File-based persistence; data survives restarts via CSV storage, no database server required

## Architecture

- **MVC pattern**: FXML views + CSS styling, six controllers, a dedicated service layer, and model classes
- **`FileStorageService`** abstracts all CSV read/write, keeping persistence concerns out of business logic
- ~1,500 lines of Java 17

## Tech Stack

Java 17 · JavaFX (FXML, CSS) · Maven

## Run Locally

```bash
mvn clean javafx:run
```

Data files are created under `hotel_data/` on first run.

## Author

Divyayan Das
