package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.Country;
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
public class CountryServiceTest extends BaseTest {

    @Autowired
    CountryService countryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (6,'EUA');");
        jdbcTemplate.execute("INSERT INTO pais (id, name) VALUES (7,'Grécia');");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM pais");
    }

    @Test
    @DisplayName("Teste inserir país")
    void insertCountryTest() {
        Country country = new Country(1, "Brasil");
        countryService.salvar(country);
        Country countryDB = countryService.findById(1);
        assertEquals(1, countryDB.getId());
        assertEquals("Brasil", countryDB.getName());
    }

    @Test
    @DisplayName("Teste alterar país")
    void updateCountryTest() {
        Country country = countryService.findById(6);
        assertEquals("EUA", country.getName());
        country.setName("Brasil");
        countryService.update(country);
        Country countryAlter = countryService.findById(6);
        assertEquals("Brasil", countryAlter.getName());
    }

    @Test
    @DisplayName("Teste remover país")
    void removeCountryTest() {
        countryService.delete(6);
        List<Country> lista = countryService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllCountryTest() {
        List<Country> lista = countryService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste buscar país por ID")
    void findByIdTest() {
        Country country = countryService.findById(6);
        assertNotNull(country);
        assertEquals(6, country.getId());
        assertEquals("EUA", country.getName());
    }

    @Test
    @DisplayName("Teste buscar país por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> countryService.findById(10));
        assertEquals("País não existe", exception.getMessage());
    }

    @Test
    @DisplayName("Teste buscar por nome")
    void findByNameStartsWithTest() {
        var lista = countryService.findByNomeEqualsIgnoreCase("EUA");
        assertEquals(1, lista.size());
    }











}
