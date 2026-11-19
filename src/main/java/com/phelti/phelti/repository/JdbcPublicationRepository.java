package com.phelti.phelti.repository;

import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
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
                    estado,
                    duracion,
                    fecha_hora
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
                    duracion,
                    fecha_hora
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
            sql.append(" AND (LOWER(titulo) LIKE ? OR LOWER(descripcion) LIKE ?)");
            String pattern = "%" + filter.keyword().toLowerCase() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
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
                duracion,
                fecha_hora
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
                duracion,
                fecha_hora
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            publication.getDurationMinutes(),
            publication.getScheduledAt() == null ? null : Timestamp.valueOf(publication.getScheduledAt())
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
                duracion = ?,
                fecha_hora = ?
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
            publication.getScheduledAt() == null ? null : Timestamp.valueOf(publication.getScheduledAt()),
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

        Timestamp scheduledAt = resultSet.getTimestamp("fecha_hora");
        publication.setScheduledAt(scheduledAt == null ? null : scheduledAt.toLocalDateTime());

        return publication;
    }
}
