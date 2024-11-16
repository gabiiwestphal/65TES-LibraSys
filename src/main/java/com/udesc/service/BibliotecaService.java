package com.udesc.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.udesc.database.DatabaseConnection;
import com.udesc.model.Livro;

public class BibliotecaService {

	public void adicionarLivro(String titulo, String autor, int ano) {
        if (ano <= 0) {
            throw new IllegalArgumentException("Ano inválido - deve ser um número positivo.");
        }
        
        if (isLivroDuplicado(titulo, autor)) {
            throw new IllegalArgumentException("Erro: Livro duplicado - já existe um livro com o mesmo título e autor.");
        }

        String sql = "INSERT INTO livros (titulo, autor, ano_publicacao) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            stmt.setInt(3, ano);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar livro: " + e.getMessage());
        }
    }

    private boolean isLivroDuplicado(String titulo, String autor) {
        String sql = "SELECT COUNT(*) FROM livros WHERE LOWER(titulo) = LOWER(?) AND LOWER(autor) = LOWER(?)";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retorna verdadeiro se houver ao menos um livro duplicado
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar duplicidade do livro: " + e.getMessage());
        }
        return false;
    }

    public boolean removerLivro(int id) {
        String sql = "DELETE FROM livros WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover livro: " + e.getMessage());
        }
    }

    public List<Livro> buscarLivros(String query) {
        String sql = "SELECT * FROM livros WHERE LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ?";
        List<Livro> resultados = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            stmt.setString(2, "%" + query.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Livro livro = new Livro(
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getInt("ano_publicacao")
                );
                livro.setId(rs.getInt("id"));
                resultados.add(livro);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar livros: " + e.getMessage());
        }
        return resultados;
    }

    public Optional<Livro> verDetalhesDoLivro(int id) {
        String sql = "SELECT * FROM livros WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Livro livro = new Livro(
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getInt("ano_publicacao")
                );
                livro.setId(rs.getInt("id"));
                return Optional.of(livro);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar detalhes do livro: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Livro> listarLivros() {
        String sql = "SELECT * FROM livros";
        List<Livro> livros = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Livro livro = new Livro(
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getInt("ano_publicacao")
                );
                livro.setId(rs.getInt("id"));
                livros.add(livro);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros: " + e.getMessage());
        }
        return livros;
    }

    public long contarLivros() {
        String sql = "SELECT COUNT(*) FROM livros";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar livros: " + e.getMessage());
        }
        return 0;
    }

    public List<Livro> listarLivrosPorAno(int ano) {
        String sql = "SELECT * FROM livros WHERE ano_publicacao = ?";
        List<Livro> livrosPorAno = new ArrayList<>();
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ano);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Livro livro = new Livro(
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getInt("ano_publicacao")
                );
                livro.setId(rs.getInt("id"));
                livrosPorAno.add(livro);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros por ano: " + e.getMessage());
        }
        return livrosPorAno;
    }

    public boolean atualizarAnoPublicacao(int id, int novoAno) {
        if (novoAno <= 0) {
            throw new IllegalArgumentException("Ano inválido - deve ser um número positivo.");
        }
        
        String sql = "UPDATE livros SET ano_publicacao = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, novoAno);
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar ano de publicação: " + e.getMessage());
        }
    }
}