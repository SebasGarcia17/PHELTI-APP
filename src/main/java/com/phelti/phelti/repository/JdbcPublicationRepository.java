package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcPublicationRepository implements PublicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcPublicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Publication> findAllPublications() {

        String sql = """
                SELECT
                    id_publicacion,
                    id_usuario,
                    id_categoria,
                    tipo,
                    titulo,
                    descripcion,
                    modalidad,
                    valor_tiempo,
                    negociable,
                    estado
                FROM publicacion
                """;

        return jdbcTemplate.query(sql, this::mapPublication);
    }

   @Override
public Optional<Publication> findPublicationById(Integer publicationId) {

    String sql = """
            SELECT
                id_publicacion,
                id_usuario,
                id_categoria,
                tipo,
                titulo,
                descripcion,
                modalidad,
                valor_tiempo,
                negociable,
                estado
            FROM publicacion
            WHERE id_publicacion = ?
            """;

    List<Publication> publications =
            jdbcTemplate.query(sql, this::mapPublication, publicationId);

    return publications.stream().findFirst();
}

    @Override
public Publication createPublication(Publication publication) {

    String sql = """
            INSERT INTO publicacion (
                id_usuario,
                id_categoria,
                tipo,
                titulo,
                descripcion,
                modalidad,
                valor_tiempo,
                negociable,
                estado
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id_publicacion
            """;

    Integer generatedId = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            publication.getUserId(),
            publication.getCategoryId(),
            publication.getPublicationType(),
            publication.getTitle(),
            publication.getDescription(),
            publication.getInteractionMode(),
            publication.getPheltAmount(),
            publication.isNegotiable(),
            publication.getPublicationStatus()
    );

    publication.setPublicationId(generatedId);

    return publication;
}

    @Override
public Publication updatePublication(Integer publicationId, Publication publication) {

    String sql = """
            UPDATE publicacion
            SET
                id_usuario = ?,
                id_categoria = ?,
                tipo = ?,
                titulo = ?,
                descripcion = ?,
                modalidad = ?,
                valor_tiempo = ?,
                negociable = ?,
                estado = ?
            WHERE id_publicacion = ?
            """;

    jdbcTemplate.update(
            sql,
            publication.getUserId(),
            publication.getCategoryId(),
            publication.getPublicationType(),
            publication.getTitle(),
            publication.getDescription(),
            publication.getInteractionMode(),
            publication.getPheltAmount(),
            publication.isNegotiable(),
            publication.getPublicationStatus(),
            publicationId
    );

    publication.setPublicationId(publicationId);

    return publication;
}

    @Override
    public void deletePublication(Integer publicationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private Publication mapPublication(ResultSet resultSet, int rowNumber) throws SQLException {

        Publication publication = new Publication();

        publication.setPublicationId(resultSet.getInt("id_publicacion"));
        publication.setUserId(resultSet.getInt("id_usuario"));
        publication.setCategoryId(resultSet.getInt("id_categoria"));

        publication.setPublicationType(resultSet.getString("tipo"));
        publication.setTitle(resultSet.getString("titulo"));
        publication.setDescription(resultSet.getString("descripcion"));
        publication.setInteractionMode(resultSet.getString("modalidad"));

        publication.setPheltAmount(resultSet.getBigDecimal("valor_tiempo"));
        publication.setNegotiable(resultSet.getBoolean("negociable"));
        publication.setPublicationStatus(resultSet.getString("estado"));

        return publication;
    }
}