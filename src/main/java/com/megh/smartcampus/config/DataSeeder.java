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
 * Runs once on startup.
 * Creates default users and seeds the campus graph (buildings, nodes, edges).
 * Safe to restart — checks before inserting.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository      userRepo;
    private final PasswordEncoder     encoder;
    private final BuildingRepository  buildingRepo;
    private final GraphNodeRepository nodeRepo;
    private final GraphEdgeRepository edgeRepo;
    private final GraphService        graphService;
    private final TrieService         trieService;

    @Override
    public void run(String... args) {
        seedUsers();
        if (buildingRepo.count() == 0) seedCampusData();
        graphService.reload();
        trieService.reload();
        log.info("Graph built. Stats: {}", graphService.stats());
    }

    // ── Users ─────────────────────────────────────────────────────────

    private void seedUsers() {
        if (!userRepo.existsByEmail("admin@smartcampus.com")) {
            userRepo.save(User.builder()
                .firstName("Campus").lastName("Admin")
                .email("admin@smartcampus.com")
                .password(encoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN).isActive(true).build());
            log.info("Admin created: admin@smartcampus.com / Admin@123");
        }
        if (!userRepo.existsByEmail("student@university.edu")) {
            userRepo.save(User.builder()
                .firstName("John").lastName("Doe")
                .email("student@university.edu")
                .password(encoder.encode("Student@123"))
                .role(Role.ROLE_STUDENT).isActive(true).build());
            log.info("Student created: student@university.edu / Student@123");
        }
    }

    // ── Campus data ───────────────────────────────────────────────────

    private void seedCampusData() {
        log.info("Seeding campus graph data...");

        // Buildings
        Building gate    = bld("Main Gate",        "GATE", Building.BuildingType.OFFICE,          50.0, 95.0);
        Building admin   = bld("Admin Block",       "ADMIN",Building.BuildingType.ADMINISTRATIVE,  50.0, 75.0);
        Building cseBld  = bld("CSE Block",         "CSE",  Building.BuildingType.ACADEMIC,         25.0, 55.0);
        Building eceBld  = bld("ECE Block",         "ECE",  Building.BuildingType.ACADEMIC,         45.0, 55.0);
        Building library = bld("Central Library",   "LIB",  Building.BuildingType.LIBRARY,          50.0, 40.0);
        Building cafe    = bld("Cafeteria",         "CAFE", Building.BuildingType.CAFETERIA,        70.0, 65.0);
        Building audi    = bld("Auditorium",        "AUDI", Building.BuildingType.AUDITORIUM,       25.0, 25.0);
        Building medical = bld("Medical Center",    "MED",  Building.BuildingType.MEDICAL,          70.0, 25.0);
        Building parking = bld("Parking Area",      "PARK", Building.BuildingType.PARKING,          50.0,100.0);
        Building washroom= bld("Washroom Block A",  "WCA",  Building.BuildingType.WASHROOM,         35.0, 45.0);

        // Graph nodes — each represents a physical point on campus
        GraphNode nGate     = node("Main Gate",            GraphNode.NodeType.GATE,              50.0, 95.0, null);
        GraphNode nParking  = node("Parking Entry",        GraphNode.NodeType.BUILDING_ENTRANCE, 50.0,100.0, parking);
        GraphNode nAdmin    = node("Admin Block Entrance", GraphNode.NodeType.BUILDING_ENTRANCE, 50.0, 75.0, admin);
        GraphNode nJunction = node("Main Junction",        GraphNode.NodeType.INTERSECTION,      50.0, 65.0, null);
        GraphNode nCse      = node("CSE Block Entrance",   GraphNode.NodeType.BUILDING_ENTRANCE, 25.0, 55.0, cseBld);
        GraphNode nEce      = node("ECE Block Entrance",   GraphNode.NodeType.BUILDING_ENTRANCE, 45.0, 55.0, eceBld);
        GraphNode nCafe     = node("Cafeteria Entrance",   GraphNode.NodeType.BUILDING_ENTRANCE, 70.0, 65.0, cafe);
        GraphNode nLib      = node("Library Entrance",     GraphNode.NodeType.BUILDING_ENTRANCE, 50.0, 40.0, library);
        GraphNode nAudi     = node("Auditorium Entrance",  GraphNode.NodeType.BUILDING_ENTRANCE, 25.0, 25.0, audi);
        GraphNode nMedical  = node("Medical Center Door",  GraphNode.NodeType.BUILDING_ENTRANCE, 70.0, 25.0, medical);
        GraphNode nWash     = node("Washroom Block A",     GraphNode.NodeType.BUILDING_ENTRANCE, 35.0, 45.0, washroom);
        GraphNode nSouth    = node("South Junction",       GraphNode.NodeType.INTERSECTION,      50.0, 25.0, null);

        // Edges — bidirectional walkable paths with distances in metres
        edge(nGate,    nParking,   60.0);
        edge(nGate,    nAdmin,    200.0);
        edge(nAdmin,   nJunction, 100.0);
        edge(nJunction,nCse,      120.0);
        edge(nJunction,nEce,       80.0);
        edge(nJunction,nCafe,     120.0);
        edge(nJunction,nLib,      150.0);
        edge(nCse,     nWash,      60.0);
        edge(nLib,     nSouth,    150.0);
        edge(nSouth,   nAudi,     120.0);
        edge(nSouth,   nMedical,  100.0);

        log.info("Campus data seeded: {} buildings, {} nodes, {} edges",
            buildingRepo.count(), nodeRepo.count(), edgeRepo.count());
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Building bld(String name, String code, Building.BuildingType type, double x, double y) {
        return buildingRepo.save(Building.builder()
            .name(name).code(code).type(type)
            .coordinateX(x).coordinateY(y).isActive(true).build());
    }

    private GraphNode node(String name, GraphNode.NodeType type, double x, double y, Building building) {
        return nodeRepo.save(GraphNode.builder()
            .name(name).nodeType(type).coordinateX(x).coordinateY(y)
            .floor(0).building(building).isActive(true).build());
    }

    private void edge(GraphNode src, GraphNode tgt, double metres) {
        edgeRepo.save(GraphEdge.builder()
            .sourceNode(src).targetNode(tgt)
            .distanceMeters(metres).pathType(GraphEdge.PathType.WALKWAY)
            .isBidirectional(true).isActive(true).build());
    }
}
