package com.aluracursos.Literalura.principal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import com.aluracursos.Literalura.model.*;
import com.aluracursos.Literalura.repository.AutoresRepository;
import com.aluracursos.Literalura.repository.LibroRepository;
import com.aluracursos.Literalura.services.ConsumoAPI;
import com.aluracursos.Literalura.services.ConvierteDatos;
public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private AutoresRepository repositorioAutores;
    private LibroRepository repositorioLibros;
    private String nombreLibro;
    String menu = """
                    Elija la opción através de su número:
                    1- Buscar libro por titulo
                    2- Listar libros registrados
                    3- Listar autores registrados
                    4- Listar autores vivos segun año
                    5- Listar libros por idioma
                    0- Salir
                    """;

    
    public Principal(AutoresRepository repositorioAutores, LibroRepository repositorioLibros) {
        this.repositorioAutores = repositorioAutores;
        this.repositorioLibros = repositorioLibros;
    }


    public void muestraMenu() {
        var opcion = -1;
        teclado = new Scanner(System.in);
        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = teclado.nextInt();
                teclado.nextLine();
            } catch (Exception e) {
                System.out.println("Opcion invalida");
                muestraMenu();
            }

            switch (opcion) {
                case 1:
                    buscarLibroApi();
                    break;
                case 2:
                    listarLibros();
                    break;
                case 3:
                    registroDeAutores();
                    break;
                case 4:
                    registroAutoresVivos();
                    break;
                case 5:
                    registroLibrosIdioma();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Ingrese una de las opciones del menu");
                    break;
            }

        }
    }
       private void buscarLibroApi() {
           DatosGenerales datosGenerales = obtenerDatosLibro();
           DatosLibro datosLibro = datosGenerales.resultados().get(0);
           Libro libro = new Libro(datosLibro);
           if (libro.getTitulo().toLowerCase().contains(nombreLibro)) {
               DatosAutor datosAutor = datosLibro.autor().get(0);
               Autor autor = new Autor(datosAutor);
               var datosTitulos = repositorioLibros.comprobacionDeExitenciaLibro();
               var datosNombreAutor = repositorioAutores.comprobacionDeExistenciaAutor();

               if (datosNombreAutor.contains(autor.getNombre())) {
                   if (datosTitulos.contains(libro.getTitulo())) {
                       System.out.println("\nNo se puede registrar el mismo libro más de una vez\n");
                   } else {
                       var id = repositorioAutores.obtenerIdAutor(autor.getNombre()).get(0);
                       Autor autor1 = repositorioAutores.findById(id).orElse(null);
                       libro.setAutor(autor1);
                       repositorioLibros.save(libro);
                       var libroRegistrado = repositorioLibros.obtenerDatosLibro(nombreLibro);
                       libroRegistrado.forEach(l -> System.out.printf("""
                           \n--------LIBRO---------
                           Titulo: %s
                           Autor: %s
                           Idioma: %s
                           Totañ de descargas: %s
                           ----------------------\n
                           """, l.getTitulo(), l.getAutor().getNombre(), l.getIdiomas().get(0), l.getNumeroDeDescargas()));
                   }
               } else {
                   repositorioAutores.save(autor);
                   libro.setAutor(autor);
                   repositorioLibros.save(libro);
                   var libroRegistrado = repositorioLibros.obtenerDatosLibro(nombreLibro);
                   libroRegistrado.forEach(l -> System.out.printf("""
                           \n--------LIBRO---------
                           Titulo: %s
                           Autor: %s
                           Idioma: %s
                           Total de descargas: %s
                           ----------------------\n
                           """, l.getTitulo(), l.getAutor().getNombre(), l.getIdiomas().get(0), l.getNumeroDeDescargas()));
               }
           } else {
               System.out.println("No se encuentra libro con este titulo");
               buscarLibroApi();
           }
       }

       private void listarLibros() {
        var registro = repositorioLibros.obtenerTituloLibro();
                   registro.forEach(l -> System.out.printf("""
                           \n--------LIBRO---------
                           Titulo: %s
                           Autor: %s
                           Idioma: %s
                           Total de descargas: %s
                           ----------------------\n
                           """, l.getTitulo(), l.getAutor().getNombre(), l.getIdiomas().get(0), l.getNumeroDeDescargas()));


       }

       private void registroDeAutores() {
        var registro = repositorioAutores.registroAutores();
           var libroAutores = repositorioLibros.obtenerTituloLibro(registro);
           Map<Autor, List<String>> librosPorAutor = libroAutores.stream()
                   .collect(Collectors.groupingBy(
                           Libro::getAutor,
                           Collectors.mapping(Libro::getTitulo, Collectors.toList())
                   ));

           librosPorAutor.forEach((autor, titulos) -> System.out.printf("""
                   \nAutor: %s
                   Fecha de nacimiento: %s
                   Fecha de fallecimiento: %s
                   Libros: [%s]\n
                   """, autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento(), String.join(", ", titulos)));
       }

       private void registroAutoresVivos() {
            int ano = 0;
            teclado = new Scanner(System.in);
           int anoActual = LocalDate.now().getYear();
           System.out.println("Escribe el año: ");
           try {
               ano = teclado.nextInt();
               teclado.nextLine();
           } catch (Exception e) {
               System.out.println("Ingresa un valor valido");
               registroAutoresVivos();
           }
           if (ano <= anoActual) {
               var nombreAutoresVivos = repositorioAutores.nombreAutoresVivos(ano);
               if (nombreAutoresVivos.isEmpty()) {
                   System.out.println("\nNo se encontraron autores vivos en ese año\n");
               } else {
                   var libroAutores = repositorioLibros.obtenerTituloLibro(nombreAutoresVivos);
                   Map<Autor, List<String>> librosPorAutor = libroAutores.stream()
                           .collect(Collectors.groupingBy(
                                   Libro::getAutor,
                                   Collectors.mapping(Libro::getTitulo, Collectors.toList())
                           ));

                   librosPorAutor.forEach((autor, titulos) -> System.out.printf("""
                           \nAutor: %s
                           Fecha nacimiento: %s
                           Fecha fallecimiento: %s
                           Libros: [%s]\n
                           """, autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento(), String.join(", ", titulos)));
               }
           } else {
               System.out.println("El año es invalido, por favor ingrese otro año");
           }

       }

       private void registroLibrosIdioma() {
           System.out.println("""
                   Ingrese el idioma para listar los libros:
                   es- español
                   en- ingles
                   fr- frances
                   pt- portugues
                   """);

           var opcion = teclado.nextLine().toLowerCase();
           if (opcion.equals("es")) {
               buscarLibrosIdioma(opcion);
           } else if (opcion.equals("en")){
               buscarLibrosIdioma(opcion);
           } else if (opcion.equals("fr")) {
               buscarLibrosIdioma(opcion);
           } else if (opcion.equals("pt")) {
               buscarLibrosIdioma(opcion);
           } else {
               System.out.println("Ingresa una opcion valida");
               registroLibrosIdioma();
           }

       }

       private void buscarLibrosIdioma(String idioma) {
            List<String> idiomas = new ArrayList<>();
            idiomas.add(idioma);
           var librosIdioma = repositorioLibros.obtenerLibroIdioma(idiomas);
           librosIdioma.forEach(l -> System.out.printf("""
                           \n--------LIBRO---------
                           Titulo: %s
                           Autor: %s
                           Idioma: %s
                           Total de descargas: %s
                           ----------------------\n
                           """, l.getTitulo(), l.getAutor().getNombre(), l.getIdiomas().get(0), l.getNumeroDeDescargas()));
       }

       private DatosGenerales obtenerDatosLibro() {
            System.out.println("Ingrese el libro que desea buscar");
            nombreLibro = teclado.nextLine().toLowerCase();
            var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "+"));
            if (json.contains("title")) {
                DatosGenerales datos = conversor.obtenerDatos(json, DatosGenerales.class);
                return datos;
            } else {
                System.out.println("Libro inexistente");
                DatosGenerales datos = obtenerDatosLibro();
                return datos;
            }


        }
    }

