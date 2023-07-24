package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
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
public class TeamServiceTest extends BaseTest {

    @Autowired
    TeamService teamService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("INSERT INTO equipe (id_quipe, nome_equipe) VALUES (6,'Team 1');");
        jdbcTemplate.execute("INSERT INTO equipe (id_quipe, nome_equipe) VALUES (7,'Team 2');");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM equipe");
    }

    @Test
    @DisplayName("Teste buscar por nome")
    void findByNameStartsWithTest() {
        var lista = teamService.findByNameIgnoreCase("Team 1");
        assertEquals(1, lista.size());
        var exception = assertThrows(
                ObjectNotFound.class, () -> teamService.findByNameIgnoreCase("Team 3"));
        assertEquals("Equipe Team 3 não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste inserir equipe")
    void insertUserTest() {
        Team team = new Team(1, "Team 3" );
        teamService.salvar(team);
        Team teamDB = teamService.findById(1);
        assertEquals("Team 3", teamDB.getName());
    }

    @Test
    @DisplayName("Teste remover equipe")
    void removeUserTest() {
        teamService.delete(6);
        List<Team> lista = teamService.listAll();
        assertEquals(1, lista.size());
        assertEquals(7, lista.get(0).getId());
    }

    @Test
    @DisplayName("Teste buscar equipe por ID")
    void findByIdTest() {
        Team team = teamService.findById(6);
        assertNotNull(team);
        assertEquals(6, team.getId());
        assertEquals("Team 1", team.getName());
    }

    @Test
    @DisplayName("Teste buscar equipe por ID inexistente")
    void findByIdNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> teamService.findById(10));
        assertEquals("Equipe 10 não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste remover equipe inexistente")
    void removeUserNonExistsTest() {
        var exception = assertThrows(
                ObjectNotFound.class, () -> teamService.delete(10));
        assertEquals("Equipe 10 não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Teste listar todos")
    void listAllUsersTest() {
        List<Team> lista = teamService.listAll();
        assertEquals(2, lista.size());
    }

    @Test
    @DisplayName("Teste listar todos sem possuir equipe cadastradas")
    void listAllUsersEmptyTest() {
        tearDown();
        var exception = assertThrows(
                ObjectNotFound.class, () -> teamService.listAll());
        assertEquals("Não existe equipes cadastradas", exception.getMessage());
    }

    @Test
    @DisplayName("Teste alterar equipe")
    void updateUsersTest() {
        Team team = teamService.findById(6);
        assertEquals("Team 1", team.getName());
        team.setName("Team 4");
        teamService.update(team);
        Team teamAlter = teamService.findById(6);
        assertEquals("Team 4", teamAlter.getName());
    }

}
