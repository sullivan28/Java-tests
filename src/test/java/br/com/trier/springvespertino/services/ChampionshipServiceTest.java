package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.Championship;
import br.com.trier.springvespertino.services.exceptions.IntegrityViolation;
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
public class ChampionshipServiceTest extends BaseTest {

    @Autowired
    ChampionshipService championshipService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (6,'Vôlei', 2023);");
        jdbcTemplate.execute("INSERT INTO campeonato (codigo_campeonato, descricao, ano) VALUES (7,'Futebol', 2024);");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM campeonato");
    }

    @Test
    @DisplayName("Teste buscar campeonato por ID")
    void findByIdTest() {
        Championship championship = championshipService.findById(6);
        assertNotNull(championship);
        assertEquals(6, championship.getId());
        assertEquals("Vôlei", championship.getDescription());
        assertEquals(2023, championship.getYear());
    }

    @Test
    @DisplayName("Teste inserir campeonato")
    void insertChampionshipTest() {
        Championship championship = new Championship(1, "karate", 2021);
        championshipService.insert(championship);
        Championship championshipDB = championshipService.findById(1);
        assertEquals(1, championshipDB.getId());
        assertEquals("karate", championshipDB.getDescription());
        assertEquals(2021, championshipDB.getYear());
    }

    @Test
    @DisplayName("Teste inserir campeonato ano invalido")
    void insertChampionshipInvalidYearTest() {
        Championship championship = new Championship(1, "karate", null);
        var exception = assertThrows(
                IntegrityViolation.class, () -> championshipService.insert(championship));
        assertEquals("Ano não pode ser nulo", exception.getMessage());

        Championship championshipTwo = new Championship(1, "karate", 1980);
        var exceptionTwo = assertThrows(
                IntegrityViolation.class, () -> championshipService.insert(championshipTwo));
        assertEquals("Ano inválido: 1980", exceptionTwo.getMessage());

        Championship championshipThree = new Championship(1, "karate", 3100);
        var exceptionThree = assertThrows(
                IntegrityViolation.class, () -> championshipService.insert(championshipThree));
        assertEquals("Ano inválido: 3100", exceptionThree.getMessage());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllChampionshipTest() {
        List<Championship> lista = championshipService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste alterar campeonato")
    void updateCountryTest() {
        Championship championship = championshipService.findById(6);
        assertEquals("Vôlei", championship.getDescription());
        championship.setDescription("karate");
        championshipService.update(championship);
        Championship championshipAlter = championshipService.findById(6);
        assertEquals("karate", championshipAlter.getDescription());
    }

    @Test
    @DisplayName("Teste remover campeonato")
    void removeCountryTest() {
        championshipService.delete(6);
        List<Championship> lista = championshipService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar por intervalo do ano ")
    void findByYearBetweenTest() {
        var lista = championshipService.findByYearBetween(2022, 2023);
        assertEquals(1, lista.size());
        var listaTwo = championshipService.findByYear(2023);
        assertEquals(1, listaTwo.size());
    }

    @Test
    @DisplayName("Teste buscar por descrição ")
    void findByDescriptionContainsIgnoreCaseTest() {
        var lista = championshipService.findByDescriptionContainsIgnoreCase("Vôlei");
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Teste buscar por descrição e ano")
    void findByescriptionContainsIgnoreCaseAndAnoEqualsTest() {
        var lista = championshipService.findByescriptionContainsIgnoreCaseAndAnoEquals("Vôlei", 2023);
        assertEquals(1, lista.size());
    }











}
