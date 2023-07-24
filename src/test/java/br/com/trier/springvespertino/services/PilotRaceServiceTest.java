package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.*;
import br.com.trier.springvespertino.services.exceptions.IntegrityViolation;
import br.com.trier.springvespertino.services.exceptions.ObjectNotFound;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class PilotRaceServiceTest extends BaseTest {

    @Autowired
    PilotRaceService pilotRaceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (6,'EUA');");
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (7,'Grécia');");
        jdbcTemplate.execute("INSERT INTO equipe (id_quipe, nome_equipe) VALUES (6,'Team 1');");
        jdbcTemplate.execute("INSERT INTO equipe (id_quipe, nome_equipe) VALUES (7,'Team 2');");
        jdbcTemplate.execute("INSERT INTO piloto (id_piloto, nome_piloto, country_id, team_id_quipe) VALUES (6,'Alisson', 6, 6);");
        jdbcTemplate.execute("INSERT INTO piloto (id_piloto, nome_piloto, country_id, team_id_quipe) VALUES (7,'Clavison', 7, 7);");

        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (6,'Alasca', 600, 6);");
        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (7,'Monte Olimpo', 700, 7);");
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (6,'F1', 2023);");
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (7,'F2', 2024);");
        jdbcTemplate.execute("INSERT INTO corrida (id_corrida, data_corrida, championship_codigo_campeonato, speedway_id_pista) VALUES (6,'2023-07-23 15:00:00', 6, 6);");
        jdbcTemplate.execute("INSERT INTO corrida (id_corrida, data_corrida, championship_codigo_campeonato, speedway_id_pista) VALUES (7,'2023-07-21 10:00:00', 7, 7);");

        jdbcTemplate.execute("INSERT INTO piloto_corrida (id, colocacao, pilot_id_piloto, race_id_corrida) VALUES (6,'1', 6, 6);");
        jdbcTemplate.execute("INSERT INTO piloto_corrida (id, colocacao, pilot_id_piloto, race_id_corrida) VALUES (7,'2', 7, 7);");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM piloto_corrida");
        jdbcTemplate.execute("DELETE FROM corrida");
        jdbcTemplate.execute("DELETE FROM campeonato");
        jdbcTemplate.execute("DELETE FROM pista");
        jdbcTemplate.execute("DELETE FROM piloto");
        jdbcTemplate.execute("DELETE FROM equipe");
        jdbcTemplate.execute("DELETE FROM pais");
    }

    @Test
    @DisplayName("Teste buscar colocação por ID")
    void findByIdTest() {
        PilotRace pilotRace = pilotRaceService.findById(6);
        assertNotNull(pilotRace);
        assertEquals(6, pilotRace.getId());
        assertEquals(1, pilotRace.getPlacement());
        assertEquals("Alisson", pilotRace.getPilot().getName());
        assertEquals(6, pilotRace.getRace().getId());
    }

    @Test
    @DisplayName("Teste buscar colocação por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findById(10));
        assertEquals("ID 10 inválido!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir pista")
    void insertPilotRaceTest() {
        Race race  = new Race(6, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2023));
        Pilot pilot = new Pilot(6, "Alisson", new Country(6, "EUA"), new Team(6, "Team 1"));
        PilotRace pilotRace  = new PilotRace(1, 3, pilot, race);
        pilotRaceService.insert(pilotRace);
        PilotRace pilotRaceDB = pilotRaceService.findById(1);
        assertEquals(1, pilotRaceDB.getId());
        assertEquals(3, pilotRaceDB.getPlacement());
        assertEquals("Alisson", pilotRaceDB.getPilot().getName());
        assertEquals(6, pilotRaceDB.getRace().getId());
    }

    @Test
    @DisplayName("Teste inserir corrida campeonato invalido")
    void insertPilotRaceInvalidPlacementTest() {
        Race race  = new Race(6, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2023));
        Pilot pilot = new Pilot(6, "Alisson", new Country(6, "EUA"), new Team(6, "Team 1"));
        PilotRace pilotRace  = new PilotRace(1, null, pilot, race);
        var exception = assertThrows(
                IntegrityViolation.class, () -> pilotRaceService.insert(pilotRace));
        assertEquals("Colocacao null!", exception.getMessage());
        pilotRace.setPlacement(0);
        var exceptionTwo = assertThrows(
                IntegrityViolation.class, () -> pilotRaceService.insert(pilotRace));
        assertEquals("Colocacao zero!", exceptionTwo.getMessage());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllPilotRaceTest() {
        List<PilotRace> lista = pilotRaceService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste listar todas sem possuir colocação cadastradas")
    void listAllPilotRaceEmptyTest() {
        tearDown();
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.listAll());
        assertEquals("Nenhum PilotoCorrida cadastrado!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste alterar colocação")
    void updatePilotRaceTest() {
        PilotRace pilotRace = pilotRaceService.findById(6);
        assertEquals(1, pilotRace.getPlacement());
        pilotRace.setPlacement(7);
        pilotRaceService.update(pilotRace);
        PilotRace pilotRaceAlter = pilotRaceService.findById(6);
        assertEquals(7, pilotRaceAlter.getPlacement());
    }

    @Test
    @DisplayName("Teste remover colocação")
    void removePilotRaceTest() {
        pilotRaceService.delete(6);
        List<PilotRace> lista = pilotRaceService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar por colocação")
    void findByPlacementTest() {
        var lista = pilotRaceService.findByPlacement(1);
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findByPlacement(10));
        assertEquals("Nenhum PilotoCorrida nesta posição!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por piloto")
    void findByPilotTest() {
        Pilot pilot = new Pilot(6, "Alisson", new Country(6, "EUA"), new Team(6, "Team 1"));
        var lista = pilotRaceService.findByPilot(pilot);
        assertEquals(1, lista.size());
        Pilot pilotTwo = new Pilot(10, "Filipe", new Country(6, "EUA"), new Team(6, "Team 1"));
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findByPilot(pilotTwo));
        assertEquals("Nenhum PilotoCorrida com esse piloto!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por corrida")
    void findByRaceOrderByPlacementAscTest() {
        Race race  = new Race(6, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2021));
        var lista = pilotRaceService.findByRaceOrderByPlacementAsc(race);
        assertEquals(1, lista.size());
        Race raceTwo  = new Race(10, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2021));
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findByRaceOrderByPlacementAsc(raceTwo));
        assertEquals("Nenhum PilotoCorrida nesta corrida!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por piloto e corrida")
    void findByPlacementBetweenAndRaceTest() {
        Pilot pilot = new Pilot(6, "Alisson", new Country(6, "EUA"), new Team(6, "Team 1"));
        Race race  = new Race(6, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2021));
        PilotRace pilotRace = pilotRaceService.findByPilotAndRace(pilot, race);
        assertNotNull(pilotRace);
        assertEquals(6, pilotRace.getId());
        assertEquals(1, pilotRace.getPlacement());
        assertEquals("Alisson", pilotRace.getPilot().getName());
        assertEquals(6, pilotRace.getRace().getId());

        Pilot pilotTwo = new Pilot(10, "Filipe", new Country(6, "EUA"), new Team(6, "Team 1"));
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findByPilotAndRace(pilotTwo, race));
        assertEquals("Nenhum PilotoCorrida com esses parâmetros de busca!", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar entre colocação")
    void findByPilotAndRacetTest() {
        Race race  = new Race(6, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2021));
        var lista = pilotRaceService.findByPlacementBetweenAndRace(1, 2, race);
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotRaceService.findByPlacementBetweenAndRace(10, 12, race));
        assertEquals("Nenhum PilotoCorrida com esses parâmetros de busca!", exception.getMessage());
    }



}
