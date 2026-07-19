package com.megh.smartcampus.config;

import com.megh.smartcampus.algorithm.graph.GraphService;
import com.megh.smartcampus.algorithm.trie.TrieService;
import com.megh.smartcampus.entity.*;
import com.megh.smartcampus.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder runs once on startup and creates default users + sample campus data.
 * Safe to restart — checks before inserting.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository       userRepo;
    private final PasswordEncoder      encoder;
    private final BuildingRepository   buildingRepo;
    private final DepartmentRepository deptRepo;
    private final GraphNodeRepository  nodeRepo;
    private final GraphEdgeRepository  edgeRepo;
    private final FacultyRepository    facultyRepo;
    private final ClassroomRepository  classroomRepo;
    private final GraphService         graphService;
    private final TrieService          trieService;

    @Override
    public void run(String... args) {
        seedUsers();
        if (buildingRepo.count() == 0) seedCampusData();
        // Always reload graph and trie after seeding so routing works immediately
        graphService.reload();
        trieService.reload();
        log.info("Graph and Trie reloaded after seeding. Stats: {}", graphService.stats());
    }

    private void seedUsers() {
        if (!userRepo.existsByEmail("admin@smartcampus.com")) {
            userRepo.save(User.builder()
                .firstName("Campus").lastName("Admin")
                .email("admin@smartcampus.com")
                .password(encoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN).isActive(true).build());
            log.info("Admin created  →  admin@smartcampus.com  /  Admin@123");
        }
        if (!userRepo.existsByEmail("student@university.edu")) {
            userRepo.save(User.builder()
                .firstName("John").lastName("Doe")
                .email("student@university.edu")
                .password(encoder.encode("Student@123"))
                .role(Role.ROLE_STUDENT).studentId("2024CS001")
                .department("Computer Science").yearOfStudy(2).isActive(true).build());
            log.info("Student created →  student@university.edu  /  Student@123");
        }
    }

    private void seedCampusData() {
        log.info("Seeding sample campus data...");

        // Departments
        Department cse = deptRepo.save(Department.builder().name("Computer Science").code("CSE")
            .headOfDepartment("Dr. R. Sharma").isActive(true).build());
        Department ece = deptRepo.save(Department.builder().name("Electronics & Communication").code("ECE")
            .headOfDepartment("Dr. P. Verma").isActive(true).build());

        // Buildings
        Building gate    = save(Building.builder().name("Main Gate").code("GATE")
            .type(Building.BuildingType.OFFICE).coordinateX(50.0).coordinateY(95.0).isActive(true).build());
        Building admin   = save(Building.builder().name("Admin Block").code("ADMIN")
            .type(Building.BuildingType.ADMINISTRATIVE).floors(3).coordinateX(50.0).coordinateY(75.0).isActive(true).build());
        Building cseBld  = save(Building.builder().name("CSE Block").code("CSE")
            .type(Building.BuildingType.ACADEMIC).floors(4).coordinateX(25.0).coordinateY(55.0).isActive(true).build());
        Building eceBld  = save(Building.builder().name("ECE Block").code("ECE")
            .type(Building.BuildingType.ACADEMIC).floors(4).coordinateX(45.0).coordinateY(55.0).isActive(true).build());
        Building library = save(Building.builder().name("Central Library").code("LIB")
            .type(Building.BuildingType.LIBRARY).floors(2).coordinateX(50.0).coordinateY(40.0).isActive(true).build());
        Building cafe    = save(Building.builder().name("Cafeteria").code("CAFE")
            .type(Building.BuildingType.CAFETERIA).floors(1).coordinateX(70.0).coordinateY(65.0).isActive(true).build());
        Building audi    = save(Building.builder().name("Auditorium").code("AUDI")
            .type(Building.BuildingType.AUDITORIUM).floors(2).coordinateX(25.0).coordinateY(25.0).isActive(true).build());
        Building medical = save(Building.builder().name("Medical Center").code("MED")
            .type(Building.BuildingType.MEDICAL).floors(1).coordinateX(70.0).coordinateY(25.0).isActive(true).build());
        Building parking = save(Building.builder().name("Parking Area").code("PARK")
            .type(Building.BuildingType.PARKING).floors(1).coordinateX(50.0).coordinateY(100.0).isActive(true).build());
        Building washroom= save(Building.builder().name("Washroom Block A").code("WCA")
            .type(Building.BuildingType.WASHROOM).floors(1).coordinateX(35.0).coordinateY(45.0).isActive(true).build());

        // Graph Nodes
        GraphNode nGate    = saveNode("Main Gate",           GraphNode.NodeType.GATE,              50.0, 95.0, 0, null);
        GraphNode nParking = saveNode("Parking Entry",       GraphNode.NodeType.BUILDING_ENTRANCE, 50.0,100.0, 0, parking);
        GraphNode nAdmin   = saveNode("Admin Block Entrance",GraphNode.NodeType.BUILDING_ENTRANCE, 50.0, 75.0, 0, admin);
        GraphNode nJunction= saveNode("Main Junction",       GraphNode.NodeType.INTERSECTION,      50.0, 65.0, 0, null);
        GraphNode nCse     = saveNode("CSE Block Entrance",  GraphNode.NodeType.BUILDING_ENTRANCE, 25.0, 55.0, 0, cseBld);
        GraphNode nEce     = saveNode("ECE Block Entrance",  GraphNode.NodeType.BUILDING_ENTRANCE, 45.0, 55.0, 0, eceBld);
        GraphNode nCafe    = saveNode("Cafeteria Entrance",  GraphNode.NodeType.BUILDING_ENTRANCE, 70.0, 65.0, 0, cafe);
        GraphNode nLib     = saveNode("Library Entrance",    GraphNode.NodeType.BUILDING_ENTRANCE, 50.0, 40.0, 0, library);
        GraphNode nAudi    = saveNode("Auditorium Entrance", GraphNode.NodeType.BUILDING_ENTRANCE, 25.0, 25.0, 0, audi);
        GraphNode nMedical = saveNode("Medical Center Door", GraphNode.NodeType.BUILDING_ENTRANCE, 70.0, 25.0, 0, medical);
        GraphNode nWash    = saveNode("Washroom Block A",    GraphNode.NodeType.BUILDING_ENTRANCE, 35.0, 45.0, 0, washroom);
        GraphNode nSouth   = saveNode("South Junction",      GraphNode.NodeType.INTERSECTION,      50.0, 25.0, 0, null);

        // Edges (bidirectional walkways)
        saveEdge(nGate, nParking, 60.0);
        saveEdge(nGate,    nAdmin,    200.0);
        saveEdge(nAdmin,   nJunction, 100.0);
        saveEdge(nJunction,nCse,      120.0);
        saveEdge(nJunction,nEce,       80.0);
        saveEdge(nJunction,nCafe,     120.0);
        saveEdge(nJunction,nLib,      150.0);
        saveEdge(nCse,     nWash,      60.0);
        saveEdge(nLib,     nSouth,    150.0);
        saveEdge(nSouth,   nAudi,     120.0);
        saveEdge(nSouth,   nMedical,  100.0);

        // Faculty
        facultyRepo.save(Faculty.builder().name("Dr. Ramesh Sharma").designation("HOD")
            .email("r.sharma@campus.edu").phone("9876540001")
            .specialization("Artificial Intelligence").cabinNumber("CSE-301").floor(3)
            .department(cse).building(cseBld).nearestNode(nCse).isAvailable(true).isActive(true).build());
        facultyRepo.save(Faculty.builder().name("Dr. Priya Singh").designation("Associate Professor")
            .email("p.singh@campus.edu").phone("9876540002")
            .specialization("Data Structures").cabinNumber("CSE-302").floor(3)
            .department(cse).building(cseBld).nearestNode(nCse).isAvailable(true).isActive(true).build());
        facultyRepo.save(Faculty.builder().name("Dr. Pooja Verma").designation("HOD")
            .email("p.verma@campus.edu").phone("9876540003")
            .specialization("VLSI Design").cabinNumber("ECE-201").floor(2)
            .department(ece).building(eceBld).nearestNode(nEce).isAvailable(true).isActive(true).build());

        // Classrooms
        classroomRepo.save(Classroom.builder().roomNumber("CSE-101").name("CSE Lecture Hall 1")
            .roomType(Classroom.RoomType.LECTURE_HALL).building(cseBld).floor(1).capacity(120)
            .nearestNode(nCse).availabilityStatus(Classroom.AvailabilityStatus.AVAILABLE)
            .hasProjector(true).hasAc(true).isActive(true).build());
        classroomRepo.save(Classroom.builder().roomNumber("CSE-201").name("Computer Lab 1")
            .roomType(Classroom.RoomType.COMPUTER_LAB).building(cseBld).floor(2).capacity(60)
            .nearestNode(nCse).availabilityStatus(Classroom.AvailabilityStatus.AVAILABLE)
            .hasProjector(true).hasAc(true).hasComputers(true).isActive(true).build());
        classroomRepo.save(Classroom.builder().roomNumber("ECE-101").name("ECE Lecture Hall")
            .roomType(Classroom.RoomType.LECTURE_HALL).building(eceBld).floor(1).capacity(100)
            .nearestNode(nEce).availabilityStatus(Classroom.AvailabilityStatus.AVAILABLE)
            .hasProjector(true).hasAc(true).isActive(true).build());

        log.info("Sample campus data seeded successfully.");
    }

    private Building save(Building b) { return buildingRepo.save(b); }

    private GraphNode saveNode(String name, GraphNode.NodeType type, double x, double y, int floor, Building b) {
        return nodeRepo.save(GraphNode.builder().name(name).nodeType(type)
            .coordinateX(x).coordinateY(y).floor(floor).building(b).isActive(true).isAccessible(true).build());
    }

    private void saveEdge(GraphNode src, GraphNode tgt, double dist) {
        edgeRepo.save(GraphEdge.builder().sourceNode(src).targetNode(tgt)
            .distanceMeters(dist).pathType(GraphEdge.PathType.WALKWAY)
            .isBidirectional(true).isActive(true).build());
    }
}
