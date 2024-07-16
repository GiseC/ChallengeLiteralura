package com.aluracursos.Literalura;

import com.aluracursos.Literalura.principal.Principal;
import com.aluracursos.Literalura.repository.AutoresRepository;
import com.aluracursos.Literalura.repository.LibroRepository;
//import com.aluracursos.Literalura.services.ConsumoAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	@Autowired
	private AutoresRepository repositorioAutores;
	@Autowired
	private LibroRepository repositorioLibros;

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal(repositorioAutores, repositorioLibros);
		principal.muestraMenu();
	}
        
}
