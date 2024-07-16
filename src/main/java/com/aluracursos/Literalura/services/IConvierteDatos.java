package com.aluracursos.Literalura.services;

public interface IConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);
}
