# SheSafe-PathFinder
# PathfinderApp 

##  Project Description
Personal safety during navigation, especially for women, is a growing concern in urban environments where traditional navigation apps overlook safety factors. **Pathfinder** addresses this gap by providing the safest routes using real-time risk analysis and offering emergency support features, ensuring secure travel for everyone — with a special focus on women's safety.
**PathfinderApp** is a Java-based GUI application designed to compute the *safest path* between two nodes in a graph using risk scores as edge weights. It simulates a real-world application like emergency evacuation planning, secure routing, or urban navigation where safety is prioritized over distance.

The graph's nodes and edges are stored in a **MySQL** database and loaded dynamically at runtime. Each edge has a **risk score** calculated from various risk parameters (`CSS`, `SLF`, `PPI`, `PPS`). 
CSS- Crime Severity Score,
SLF- Street Light Factor,
PPI- Police Present Index,
PPS- Public Survelliance Score,
Using **Dijkstra's algorithm**, the app determines the path with the lowest total risk.

With an intuitive **Java Swing GUI**, users can:

- Enter source and destination nodes
- View the safest route and its total risk score
- Trigger **Emergency Mode** to view local emergency contacts
- Rate the safety of locations
- View the **Top 5 Safest Locations** as rated by users

---

##  Features

###  Safest Path Finder
- Computes the safest path between any two locations based on cumulative risk score.
- Utilizes Dijkstra’s algorithm to ensure optimal path selection.

###  Emergency Mode
- Users can click an **"Emergency" button**.
- Based on the user’s **current location**, emergency contacts (like police, hospital, fire station) for that area are fetched from the database and displayed.

###  Location Safety Rating
- Users can **rate the safety** of a location (1 to 5 stars).
- Ratings are stored in the database.
- A section displays the **Top 5 Safest Locations** based on average user ratings.

###  User-Friendly Interface
- Java Swing-based interface for easy input and visual feedback.
- Scrollable result area with clearly formatted output.

---

##  Data Structures Used

### 1. **Graph (Adjacency List)**
- `Map<String, List<Edge>>`: Represents the entire network of nodes and edges.
- Efficient for traversal and neighbor lookups in sparse graphs.

### 2. **Edge**
- Fields: `to` (destination node), `riskScore` (sum of multiple factors)
- Each connection between nodes is weighted with a cumulative risk score.

### 3. **Priority Queue**
- Used in Dijkstra’s algorithm to always pick the node with the current lowest risk.
- Efficient pathfinding with dynamic updates of minimum scores.

### 4. **HashMaps**
- `dist`: Stores minimum risk from source to all other nodes.
- `prev`: Stores the predecessor node for reconstructing the path.

---

##  Technologies Used

- **Java 11+**
- **Swing (GUI)**
- **JDBC (Java Database Connectivity)**
- **MySQL**


#  PathFinder Database Schema

PathFinder is a safety-focused navigation system designed to guide users through the safest routes within a city or region. It combines real-time crowd-sourced data, emergency services integration, and smart pathfinding algorithms to ensure user safety during travel.

This repository contains the **MySQL database schema** used by the PathFinder system. Below is a breakdown of all tables, their relationships, and how they support the overall functionality of the platform.

---

##  Tables Overview

### 1. `nodes`
Represents key locations or landmarks (e.g., intersections, known places) in the pathfinding network.

| Field      | Type          | Description                    |
|------------|---------------|--------------------------------|
| NodeID     | INT (PK)      | Unique identifier for a node   |
| NodeName   | VARCHAR(100)  | Human-readable name            |
| Latitude   | DOUBLE        | Geographic coordinate (lat)    |
| Longitude  | DOUBLE        | Geographic coordinate (long)   |

---

### 2. `edges`
Represents the connections between nodes (roads, paths, etc.), including various safety and environmental parameters.

| Field    | Type    | Description                                      |
|----------|---------|--------------------------------------------------|
| EdgeID   | INT (PK)| Unique edge identifier                           |
| FromNode | INT (FK)| Source node ID                                   |
| ToNode   | INT (FK)| Destination node ID                              |
| Distance | DOUBLE  | Distance between nodes (in meters)               |
| CSS      | DOUBLE  | Crowd Safety Score (based on live crowding)      |
| SLF      | DOUBLE  | Street Light Factor (lighting intensity level)   |
| PPI      | DOUBLE  | Police Presence Index                            |
| PPS      | DOUBLE  | Past Police Station proximity safety score       |

---

### 3. `policestations`
Stores locations of police stations mapped to nodes.

| Field           | Type          | Description                       |
|-----------------|---------------|-----------------------------------|
| station_id      | INT (PK)      | Unique ID for each police station|
| station_name    | VARCHAR(100)  | Name of the station               |
| station_node_id | INT (FK)      | Linked NodeID location            |

---

### 4. `emergencycontacts`
Contacts linked to locations for help during emergencies (e.g., wardens, security, local help centers).

| Field         | Type          | Description                        |
|---------------|---------------|------------------------------------|
| contact_id    | INT (PK)      | Unique ID for contact              |
| node_id       | INT (FK)      | Location node                      |
| contact_name  | VARCHAR(100)  | Name of emergency contact          |
| phone_number  | VARCHAR(15)   | Phone number                       |

---

### 5. `emergencylogs`
Stores emergency reports by users, including location and nearest station for rapid action.

| Field              | Type        | Description                           |
|--------------------|-------------|---------------------------------------|
| log_id             | INT (PK)    | Unique emergency log ID               |
| user_id            | VARCHAR(50) | Identifier of the user reporting      |
| latitude           | FLOAT       | Latitude where the emergency occurred |
| longitude          | FLOAT       | Longitude of the incident             |
| nearest_station_id | INT (FK)    | Closest police station ID             |

---

### 6. `userratings`
Allows users to crowdsource feedback on safety of various locations.

| Field         | Type            | Description                                 |
|---------------|------------------|---------------------------------------------|
| rating_id     | INT (PK)         | Unique ID for the rating entry              |
| node_id       | INT (FK)         | Node being rated                            |
| user_id       | VARCHAR(50)      | Identifier of the user submitting the rating|
| safety_rating | ENUM('Safe', 'Unsafe', 'Neutral') | User's perception of safety at the node |

---

## 🛠️ Create Table SQL Commands


CREATE TABLE nodes (
  NodeID INT PRIMARY KEY AUTO_INCREMENT,
  NodeName VARCHAR(100),
  Latitude DOUBLE,
  Longitude DOUBLE
);

CREATE TABLE edges (
  EdgeID INT PRIMARY KEY AUTO_INCREMENT,
  FromNode INT,
  ToNode INT,
  Distance DOUBLE,
  CSS DOUBLE,
  SLF DOUBLE,
  PPI DOUBLE,
  PPS DOUBLE,
  FOREIGN KEY (FromNode) REFERENCES nodes(NodeID),
  FOREIGN KEY (ToNode) REFERENCES nodes(NodeID)
);

CREATE TABLE policestations (
  station_id INT PRIMARY KEY AUTO_INCREMENT,
  station_name VARCHAR(100),
  station_node_id INT,
  FOREIGN KEY (station_node_id) REFERENCES nodes(NodeID)
);

CREATE TABLE emergencycontacts (
  contact_id INT PRIMARY KEY AUTO_INCREMENT,
  node_id INT,
  contact_name VARCHAR(100),
  phone_number VARCHAR(15),
  FOREIGN KEY (node_id) REFERENCES nodes(NodeID)
);

CREATE TABLE emergencylogs (
  log_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(50),
  latitude FLOAT,
  longitude FLOAT,
  nearest_station_id INT,
  FOREIGN KEY (nearest_station_id) REFERENCES policestations(station_id)
);

CREATE TABLE userratings (
  rating_id INT PRIMARY KEY AUTO_INCREMENT,
  node_id INT,
  user_id VARCHAR(50),
  safety_rating ENUM('Safe', 'Unsafe', 'Neutral'),
  FOREIGN KEY (node_id) REFERENCES nodes(NodeID)
);

