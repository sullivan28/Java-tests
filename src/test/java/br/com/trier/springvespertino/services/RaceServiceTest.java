package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.Championship;
import br.com.trier.springvespertino.models.Country;
import br.com.trier.springvespertino.models.Race;
import br.com.trier.springvespertino.models.Speedway;
import br.com.trier.springvespertino.services.exceptions.IntegrityViolation;
import br.com.trier.springvespertino.services.exceptions.ObjectNotFound;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class RaceServiceTest extends BaseTest {

    @Autowired
    RaceService raceService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (6,'EUA');");
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (7,'Grécia');");
        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (6,'Alasca', 600, 6);");
        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (7,'Monte Olimpo', 700, 7);");
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (6,'F1', 2023);");
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (7,'F2', 2024);");
        jdbcTemplate.execute("INSERT INTO corrida (id_corrida, data_corrida, championship_codigo_campeonato, speedway_id_pista) VALUES (6,'2023-07-23 15:00:00', 6, 6);");
        jdbcTemplate.execute("INSERT INTO corrida (id_corrida, data_corrida, championship_codigo_campeonato, speedway_id_pista) VALUES (7,'2023-07-21 10:00:00', 7, 7);");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM corrida");
        jdbcTemplate.execute("DELETE FROM campeonato");
        jdbcTemplate.execute("DELETE FROM pista");
        jdbcTemplate.execute("DELETE FROM pais");
    }

    @Test
    @DisplayName("Teste buscar pista por ID")
    void findByIdTest() {
        Race race = raceService.findById(6);
        assertNotNull(race);
        assertEquals(6, race.getId());
        assertEquals(6, race.getSpeedway().getId());
        assertEquals("Alasca", race.getSpeedway().getName());
        assertEquals(6, race.getChampionship().getId());
        assertEquals("F1", race.getChampionship().getDescription());
    }

    @Test
    @DisplayName("Teste buscar corrida por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> raceService.findById(10));
        assertEquals("Corrida 10 não existe", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir pista")
    void insertRaceTest() {
        Race race  = new Race(1, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2023));
        raceService.insert(race);
        Race raceDB = raceService.findById(1);
        assertEquals(1, raceDB.getId());
        assertEquals(6, race.getSpeedway().getId());
        assertEquals("F8 Race", race.getSpeedway().getName());
        assertEquals(6, race.getChampionship().getId());
        assertEquals("F8", race.getChampionship().getDescription());
    }

    @Test
    @DisplayName("Teste inserir corrida campeonato invalido")
    void insertRaceInvalidChampionshipTest() {
        Race race  = new Race(1, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), null);
        var exception = assertThrows(
                IntegrityViolation.class, () -> raceService.insert(race));
        assertEquals("Campeonato não pode ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir corrida data invalido")
    void insertRaceInvalidDateTest() {
        Race race  = new Race(1, null, new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2023));
        var exception = assertThrows(
                IntegrityViolation.class, () -> raceService.insert(race));
        assertEquals("Data inválida", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir corrida data invalido")
    void insertRaceDiffChampionshipYearTest() {
        Race race  = new Race(1, ZonedDateTime.now(), new Speedway(6, "F8 Race", 800, new Country()), new Championship(6, "F8", 2021));
        var exception = assertThrows(
                IntegrityViolation.class, () -> raceService.insert(race));
        assertEquals("Ano da corrida diferente do ano do campeonato", exception.getMessage());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllRacesTest() {
        List<Race> lista = raceService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste listar todas sem possuir corrida cadastradas")
    void listAllRacesEmptyTest() {
        tearDown();
        var exception = assertThrows(
                ObjectNotFound.class, () -> raceService.listAll());
        assertEquals("Não existem corridas cadastradas", exception.getMessage());
    }

    @Test
    @DisplayName("Teste alterar pista")
    void updateSpeedwayTest() {
        Race race = raceService.findById(6);
        assertEquals("Alasca", race.getSpeedway().getName());
        assertEquals("F1", race.getChampionship().getDescription());
        race.setChampionship(new Championship(7, "F8", 2023));
        raceService.update(race);
        Race raceAlter = raceService.findById(6);
        assertEquals(7, raceAlter.getChampionship().getId());
        assertEquals("F8", raceAlter.getChampionship().getDescription());
    }

    @Test
    @DisplayName("Teste remover corrida")
    void removeRacesTest() {
        raceService.delete(6);
        List<Race> lista = raceService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar por data")
    void findByDateTest() {
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 7, 23, 15, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        var lista = raceService.findByDate(dateTime);
        assertEquals(1, lista.size());
        ZonedDateTime dateTimeTwo = ZonedDateTime.of(2023, 7, 21, 15, 0, 0, 0, ZoneId.of("America/Sao_Paulo"));
        var exception = assertThrows(
                ObjectNotFound.class, () -> raceService.findByDate(dateTimeTwo));
        assertEquals("Não existe corrida para a data especificada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por pista")
    void findBySpeedwayTest() {
        Speedway speedway = new Speedway(6, "Alasca", 600, new Country());
        var lista = raceService.findBySpeedway(speedway);
        assertEquals(1, lista.size());
        Speedway speedwayTwo = new Speedway(10, "Rio", 500, new Country());
        var exception = assertThrows(
                ObjectNotFound.class, () -> raceService.findBySpeedway(speedwayTwo));
        assertEquals("Não existe corrida na pista especificada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por campeonato")
    void findByChampionshipTest() {
        Championship championship = new Championship(6, "F1", 2023);
        var lista = raceService.findByChampionship(championship);
        assertEquals(1, lista.size());
        Championship championshipTwo = new Championship(10, "F9", 2021);
        var exception = assertThrows(
                ObjectNotFound.class, () -> raceService.findByChampionship(championshipTwo));
        assertEquals("Não existe corrida para o campeonato especificado", exception.getMessage());
    }








}
