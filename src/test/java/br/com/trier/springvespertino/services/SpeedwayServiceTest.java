package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.Country;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class SpeedwayServiceTest extends BaseTest {

    @Autowired
    SpeedwayService speedwayService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (6,'EUA');");
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (7,'Grécia');");
        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (6,'Alasca', 600, 6);");
        jdbcTemplate.execute("INSERT INTO pista (id_pista, nome_pista, tamanho_pista, country_id) VALUES (7,'Monte Olimpo', 700, 7);");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM pista");
        jdbcTemplate.execute("DELETE FROM pais");
    }

    @Test
    @DisplayName("Teste buscar pista por ID")
    void findByIdTest() {
        Speedway speedway = speedwayService.findById(6);
        assertNotNull(speedway);
        assertEquals(6, speedway.getId());
        assertEquals("Alasca", speedway.getName());
    }

    @Test
    @DisplayName("Teste buscar pista por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> speedwayService.findById(10));
        assertEquals("Pista 10 não existe", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir pista")
    void insertSpeedwayTest() {
        Speedway speedway = new Speedway(1, "Rio", 800, new Country());
        speedwayService.insert(speedway);
        Speedway speedwayDB = speedwayService.findById(1);
        assertEquals(1, speedwayDB.getId());
        assertEquals("Rio", speedwayDB.getName());
        assertEquals(800, speedwayDB.getSize());
    }

    @Test
    @DisplayName("Teste inserir pista tamanho invalido")
    void insertSpeedwaySizeInvalidTest() {
        Speedway speedway = new Speedway(1, "Rio", null, new Country());
        var exception = assertThrows(
                IntegrityViolation.class, () -> speedwayService.insert(speedway));
        assertEquals("Tamanho da pista inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllSpeedwaysTest() {
        List<Speedway> lista = speedwayService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste listar todas sem possuir pistas cadastradas")
    void listAllSpeedwaysEmptyTest() {
        tearDown();
        var exception = assertThrows(
                ObjectNotFound.class, () -> speedwayService.listAll());
        assertEquals("Nenhuma pista cadastrada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste alterar pista")
    void updateSpeedwayTest() {
        Speedway speedway = speedwayService.findById(6);
        assertEquals("Alasca", speedway.getName());
        speedway.setName("Alasca 02");
        speedway.setSize(900);
        speedwayService.update(speedway);
        Speedway speedwayAlter = speedwayService.findById(6);
        assertEquals("Alasca 02", speedwayAlter.getName());
        assertEquals(900, speedwayAlter.getSize());
    }

    @Test
    @DisplayName("Teste remover pista")
    void removeSpeedwayTest() {
        speedwayService.delete(6);
        List<Speedway> lista = speedwayService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar por nome")
    void findByNameStartsWithTest() {
        var lista = speedwayService.findByNameStartsWithIgnoreCase("Alasca");
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> speedwayService.findByNameStartsWithIgnoreCase("Rio"));
        assertEquals("Nenhuma pista cadastrada com esse nome", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por tamanho")
    void findBySizeStartsWithTest() {
        var lista = speedwayService.findBySizeBetween(500, 600);
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> speedwayService.findBySizeBetween(100, 200));
        assertEquals("Nenhuma pista cadastrada com essas medidas", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por país")
    void findByCountryOrderBySizeDescTest() {
        Country country =  new Country(6, "EUA");
        var lista = speedwayService.findByCountryOrderBySizeDesc(country);
        assertEquals(1, lista.size());
        Country countryTwo =  new Country(10, "Canada");
        var exception = assertThrows(
                ObjectNotFound.class, () -> speedwayService.findByCountryOrderBySizeDesc(countryTwo));
        assertEquals("Nenhuma pista cadastrada no país: Canada", exception.getMessage());
    }






}
