package br.com.trier.springvespertino.services;

import br.com.trier.springvespertino.BaseTest;
import br.com.trier.springvespertino.models.User;
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
public class UserServiceTest extends BaseTest{
	
	@Autowired
	UserService userService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void setUp() {
		// Script SQL para ser executado antes de cada teste
		jdbcTemplate.execute("INSERT INTO usuario(id_usuario, nome_usuario, email_usuario, senha_usuario, permissoes_usuario) VALUES(3, 'User 1', 'email1', 'senha1', 'ADMIN,USER');");
		jdbcTemplate.execute("INSERT INTO usuario(id_usuario, nome_usuario, email_usuario, senha_usuario, permissoes_usuario) VALUES(4, 'User 2', 'email2', 'senha2', 'USER');");
		// Adicione mais comandos SQL conforme necessário
	}

	@AfterEach
	public void tearDown() {
		// Script SQL para ser executado após cada teste
		jdbcTemplate.execute("DELETE FROM usuario");
	}
	
	@Test
	@DisplayName("Teste buscar usuário por ID")
	void findByIdTest() {
		var usuario = userService.findById(3);
		assertNotNull(usuario);
		assertEquals(3, usuario.getId());
		assertEquals("User 1", usuario.getName());
		assertEquals("email1", usuario.getEmail());
		assertEquals("senha1", usuario.getPassword());
	}
	
	@Test
	@DisplayName("Teste buscar usuário por ID inexistente")
	void findByIdNonExistsTest() {
		var exception = assertThrows(
				ObjectNotFound.class, () -> userService.findById(10));
		assertEquals("O usuário 10 não existe", exception.getMessage());
	}
	
	@Test
	@DisplayName("Teste inserir usuário")
	void insertUserTest() {
		User usuario = new User(null, "insert", "insert", "insert", "ADMIN" );
		userService.insert(usuario);
		usuario = userService.findById(1);
		assertEquals(1, usuario.getId());
		assertEquals("insert", usuario.getName());
		assertEquals("insert", usuario.getEmail());
		assertEquals("insert", usuario.getPassword());
	}
	
	@Test
	@DisplayName("Teste inserir usuário com e-mail duplicado")
	void insertUserDuplicatedEmailTest() {
		User usuario = new User(null, "insert", "email1", "insert", "ADMIN" );
		var exception = assertThrows(
				IntegrityViolation.class, () -> userService.insert(usuario));
		assertEquals("Email já existente: email1", exception.getMessage());
	}
	
	@Test
	@DisplayName("Teste remover usuário")
	void removeUserTest() {
		userService.delete(3);
		List<User> lista = userService.listAll();
		assertEquals(1, lista.size());
		assertEquals(4, lista.get(0).getId());
	}
	
	@Test
	@DisplayName("Teste remover usuário inexistente")
	void removeUserNonExistsTest() {
		var exception = assertThrows(
				ObjectNotFound.class, () -> userService.delete(10));
		assertEquals("O usuário 10 não existe", exception.getMessage());
		
	}
	
	@Test
	@DisplayName("Teste listar todos")
	void listAllUsersTest() {
		List<User> lista = userService.listAll();
		assertEquals(2, lista.size());
	}
	
	@Test
	@DisplayName("Teste listar todos sem possuir usuários cadastrados")
	void listAllUsersEmptyTest() {
		tearDown();
		var exception = assertThrows(
				ObjectNotFound.class, () -> userService.listAll());
		assertEquals("Nenhum usuário cadastrado", exception.getMessage());
	}
	
	@Test
	@DisplayName("Teste alterar usuário")
	void updateUsersTest() {
		var usuario = userService.findById(3);
		assertEquals("User 1", usuario.getName());
		var usuarioAltera = new User(3,"altera", "altera", "altera", "ADMIN");
		userService.update(usuarioAltera);
		usuario = userService.findById(3);
		assertEquals("altera", usuario.getName());
	}
	
	@Test
	@DisplayName("Teste alterar usuário com e-mail duplicado")
	void updateUsersDuplicatedEmailTest() {
		var usuarioAltera = new User(3,"altera", "email2", "altera", "ADMIN");
		var exception = assertThrows(
				IntegrityViolation.class, () -> userService.update(usuarioAltera));
		assertEquals("Email já existente: email2", exception.getMessage());
	}
	
	@Test
	@DisplayName("Teste alterar usuário inexistente")
	void updateUsersNonExistsTest() {
		var usuarioAltera = new User(1,"altera", "altera", "altera", "ADMIN");
		var exception = assertThrows(
				ObjectNotFound.class, () -> userService.update(usuarioAltera));
		assertEquals("O usuário 1 não existe", exception.getMessage());
		
	}
	
	@Test
	@DisplayName("Teste buscar por nome que inicia com")
	void findByNameStartsWithTest() {
		var lista = userService.findByName("u");
		assertEquals(2, lista.size());
		lista = userService.findByName("User 1");
		assertEquals(1, lista.size());
		var exception = assertThrows(
				ObjectNotFound.class, () -> userService.findByName("c"));
		assertEquals("Nenhum nome de usuário inicia com c", exception.getMessage());
	}
	
}
