package com.phelti.phelti.repository;

import com.phelti.phelti.model.Proposal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcProposalRepository implements ProposalRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProposalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Proposal> findAllProposals() {
        String sql = """
                SELECT
                    id_propuesta,
                    id_publicacion,
                    id_usuario_proponente,
                    mensaje,
                    estado,
                    fecha_creacion
                FROM propuesta
                ORDER BY id_propuesta ASC
                """;

        return jdbcTemplate.query(sql, this::mapProposal);
    }

    @Override
    public Optional<Proposal> findProposalById(Integer proposalId) {
        String sql = """
                SELECT
                    id_propuesta,
                    id_publicacion,
                    id_usuario_proponente,
                    mensaje,
                    estado,
                    fecha_creacion
                FROM propuesta
                WHERE id_propuesta = ?
                """;

        List<Proposal> proposals = jdbcTemplate.query(sql, this::mapProposal, proposalId);
        return proposals.stream().findFirst();
    }

    @Override
    public List<Proposal> findProposalsByPublicationId(Integer publicationId) {
        String sql = """
                SELECT
                    id_propuesta,
                    id_publicacion,
                    id_usuario_proponente,
                    mensaje,
                    estado,
                    fecha_creacion
                FROM propuesta
                WHERE id_publicacion = ?
                ORDER BY id_propuesta ASC
                """;

        return jdbcTemplate.query(sql, this::mapProposal, publicationId);
    }

    @Override
    public Proposal createProposal(Proposal proposal) {
        String sql = """
                INSERT INTO propuesta (
                    id_publicacion,
                    id_usuario_proponente,
                    mensaje,
                    estado
                )
                VALUES (?, ?, ?, ?)
                RETURNING id_propuesta, fecha_creacion
                """;

        List<Map<String, Object>> generated = jdbcTemplate.queryForList(
                sql,
                proposal.getPublicationId(),
                proposal.getProposingUserId(),
                proposal.getMessage(),
                proposal.getProposalStatus()
        );

        Map<String, Object> row = generated.get(0);
        proposal.setProposalId((Integer) row.get("id_propuesta"));
        proposal.setCreatedAt(((Timestamp) row.get("fecha_creacion")).toInstant());

        return proposal;
    }

    @Override
    public void updateProposalStatus(Integer proposalId, String status) {
        String sql = """
                UPDATE propuesta
                SET estado = ?
                WHERE id_propuesta = ?
                """;

        jdbcTemplate.update(sql, status, proposalId);
    }

    @Override
    public void deleteProposal(Integer proposalId) {
        String sql = """
                DELETE FROM propuesta
                WHERE id_propuesta = ?
                """;

        jdbcTemplate.update(sql, proposalId);
    }

    private Proposal mapProposal(ResultSet resultSet, int rowNumber) throws SQLException {
        Proposal proposal = new Proposal();

        proposal.setProposalId(resultSet.getInt("id_propuesta"));
        proposal.setPublicationId(resultSet.getInt("id_publicacion"));
        proposal.setProposingUserId(resultSet.getInt("id_usuario_proponente"));
        proposal.setMessage(resultSet.getString("mensaje"));
        proposal.setProposalStatus(resultSet.getString("estado"));
        proposal.setCreatedAt(resultSet.getTimestamp("fecha_creacion").toInstant());

        return proposal;
    }
}
