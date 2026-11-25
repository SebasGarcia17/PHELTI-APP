package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPublicationRepository implements PublicationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void lockPublication(Integer publicationId) {
        jdbcTemplate.queryForList("SELECT id_publicacion FROM publicacion WHERE id_publicacion = ? FOR UPDATE", Integer.class, publicationId);
    }

    @Override
    public boolean hasPendingProposals(Integer publicationId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM propuesta WHERE id_publicacion = ? AND estado = 'PENDING')", Boolean.class, publicationId));
    }

    @Override
    public void rejectPendingProposals(Integer publicationId) {
        jdbcTemplate.update("UPDATE propuesta SET estado = 'REJECTED' WHERE id_publicacion = ? AND estado = 'PENDING'", publicationId);
    }

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
                    estado,
                    duracion
                FROM publicacion
                """;

        return jdbcTemplate.query(sql, this::mapPublication);
    }

    @Override
    public List<Publication> searchPublications(PublicationFilter filter) {

        StringBuilder sql = new StringBuilder("""
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
                    estado,
                    duracion
                FROM publicacion
                WHERE estado = ?
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(filter.publicationStatus());

        if (filter.publicationType() != null) {
            sql.append(" AND tipo = ?");
            parameters.add(filter.publicationType());
        }

        if (filter.interactionMode() != null) {
            sql.append(" AND modalidad = ?");
            parameters.add(filter.interactionMode());
        }

        if (filter.categoryId() != null) {
            sql.append(" AND id_categoria = ?");
            parameters.add(filter.categoryId());
        }

        if (filter.maxPheltAmount() != null) {
            sql.append(" AND valor_tiempo <= ?");
            parameters.add(filter.maxPheltAmount());
        }

        if (filter.keyword() != null) {
            sql.append(" AND (strpos(LOWER(titulo), LOWER(?)) > 0 OR strpos(LOWER(descripcion), LOWER(?)) > 0)");
            parameters.add(filter.keyword());
            parameters.add(filter.keyword());
        }

        sql.append(" ORDER BY id_publicacion DESC");

        return jdbcTemplate.query(sql.toString(), this::mapPublication, parameters.toArray());
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
                estado,
                duracion
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
                estado,
                duracion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            publication.getPublicationStatus(),
            publication.getDurationMinutes()
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
                estado = ?,
                duracion = ?
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
            publication.getDurationMinutes(),
            publicationId
    );

    publication.setPublicationId(publicationId);

    return publication;
}

    @Override
public void deletePublication(Integer publicationId) {

    String sql = """
            DELETE FROM publicacion
            WHERE id_publicacion = ?
            """;

    jdbcTemplate.update(sql, publicationId);
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
        publication.setDurationMinutes(resultSet.getObject("duracion", Integer.class));


        return publication;
    }
}
