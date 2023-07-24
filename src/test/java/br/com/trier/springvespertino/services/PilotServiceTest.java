package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.Country;
import br.com.trier.springvespertino.models.Pilot;
import br.com.trier.springvespertino.models.Team;
import br.com.trier.springvespertino.services.exceptions.ObjectNotFound;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class PilotServiceTest extends BaseTest {

    @Autowired
    PilotService pilotService;

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
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM piloto");
        jdbcTemplate.execute("DELETE FROM equipe");
        jdbcTemplate.execute("DELETE FROM pais");
    }

    @Test
    @DisplayName("Teste buscar piloto por ID")
    void findByIdTest() {
        Pilot pilot = pilotService.findById(6);
        assertNotNull(pilot);
        assertEquals(6, pilot.getId());
        assertEquals("Alisson", pilot.getName());
        assertEquals(6, pilot.getTeam().getId());
        assertEquals("Team 1", pilot.getTeam().getName());
        assertEquals(6, pilot.getCountry().getId());
        assertEquals("EUA", pilot.getCountry().getName());
    }

    @Test
    @DisplayName("Teste buscar piloto por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotService.findById(10));
        assertEquals("Pilot 10 não existe", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir pista")
    void insertPilotTest() {
        Pilot pilot = new Pilot(1, "Filipe Massa", new Country(6, "EUA"), new Team(6, "Team 1"));
        pilotService.insert(pilot);
        Pilot pilotDB = pilotService.findById(1);
        assertEquals(1, pilotDB.getId());
        assertEquals("Filipe Massa", pilotDB.getName());
        assertEquals(6, pilotDB.getTeam().getId());
        assertEquals("Team 1", pilotDB.getTeam().getName());
        assertEquals(6, pilotDB.getCountry().getId());
        assertEquals("EUA", pilotDB.getCountry().getName());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllPilotTest() {
        List<Pilot> lista = pilotService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste listar todas sem possuir pilotp cadastradas")
    void listAllSpeedwaysEmptyTest() {
        tearDown();
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotService.listAll());
        assertEquals("Nenhum piloto cadastrado", exception.getMessage());
    }

    @Test
    @DisplayName("Teste alterar pilotp")
    void updatePilotTest() {
        Pilot pilot = pilotService.findById(6);
        assertEquals("Alisson", pilot.getName());
        pilot.setName("Alisson 007");
        pilotService.update(pilot);
        Pilot pilotAlter = pilotService.findById(6);
        assertEquals("Alisson 007", pilotAlter.getName());
    }

    @Test
    @DisplayName("Teste remover pista")
    void removePilotTest() {
        pilotService.delete(6);
        List<Pilot> lista = pilotService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar por nome")
    void findByNameStartsWithTest() {
        var lista = pilotService.findByNameStartsWithIgnoreCase("Alisson");
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotService.findByNameStartsWithIgnoreCase("Felipe"));
        assertEquals("Nenhum piloto com esse nome", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por país")
    void findByCountryOrderBySizeDescTest() {
        Country country =  new Country(6, "EUA");
        var lista = pilotService.findByCountry(country);
        assertEquals(1, lista.size());
        Country countryTwo =  new Country(10, "Canada");
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotService.findByCountry(countryTwo));
        assertEquals("Nenhum piloto nesse país", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por equipe")
    void findByTeamOrderBySizeDescTest() {
        Team team =  new Team(6, "Team 1");
        var lista = pilotService.findByTeam(team);
        assertEquals(1, lista.size());
        Team teamTwo =  new Team(10, "Team 3");
        var exception = assertThrows(
                ObjectNotFound.class, () -> pilotService.findByTeam(teamTwo));
        assertEquals("Nenhum piloto nesse time", exception.getMessage());
    }






}
