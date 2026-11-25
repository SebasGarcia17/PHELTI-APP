package com.phelti.phelti.service;

import com.phelti.phelti.exception.InvalidPublicationDataException;
import com.phelti.phelti.exception.PublicationNotFoundException;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationFilter;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.model.PublicationType;
import com.phelti.phelti.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import com.phelti.phelti.model.BusinessRules;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;

    private final WalletService walletService;

    public PublicationServiceImpl(PublicationRepository publicationRepository, WalletService walletService) {
        this.walletService = walletService;
        this.publicationRepository = publicationRepository;
    }

    @Override
    @Transactional
    public Publication createPublication(Publication publication) {
        if (!isBlank(publication.getPublicationStatus()) && !PublicationStatus.PUBLISHED.name().equals(publication.getPublicationStatus())) {
            throw new InvalidPublicationDataException("Una publicación nueva debe comenzar publicada.");
        }
        publication.setPublicationStatus(PublicationStatus.PUBLISHED.name());
        validatePublication(publication);
        return publicationRepository.createPublication(publication);
    }

    @Override
    public Publication findPublicationById(Integer publicationId) {
        return publicationRepository.findPublicationById(publicationId)
                .orElseThrow(() -> new PublicationNotFoundException(publicationId));
    }

    @Override
    public List<Publication> findAllPublications() {
        return publicationRepository.findAllPublications();
    }

    @Override
    public List<Publication> searchPublications(PublicationFilter filter) {
        String type = normalizeFilter(filter.publicationType());
        String mode = normalizeFilter(filter.interactionMode());
        String status = normalizeFilter(filter.publicationStatus());
        String keyword = isBlank(filter.keyword()) ? null : filter.keyword().trim();

        if (type != null && !isValidEnumValue(PublicationType.class, type)) {
            throw new InvalidPublicationDataException(
                    "Selecciona un tipo de publicación válido.");
        }

        if (mode != null && !isValidEnumValue(InteractionMode.class, mode)) {
            throw new InvalidPublicationDataException(
                    "Selecciona una modalidad válida.");
        }

        if (status == null) {
            status = PublicationStatus.PUBLISHED.name();
        }
        validateStatus(status);

        if (filter.maxPheltAmount() != null
                && filter.maxPheltAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPublicationDataException("El importe máximo debe ser mayor que cero.");
        }

        PublicationFilter normalized = new PublicationFilter(
                type,
                mode,
                filter.categoryId(),
                keyword,
                filter.maxPheltAmount(),
                status
        );

        return publicationRepository.searchPublications(normalized);
    }

    @Override
    @Transactional
    public Publication updatePublication(Integer publicationId, Publication publication) {
        lockPublication(publicationId);
        Publication existing = findPublicationById(publicationId);
        if (!PublicationStatus.PUBLISHED.name().equals(existing.getPublicationStatus())) {
            throw new InvalidPublicationDataException("Solo puedes editar publicaciones disponibles.");
        }
        if (!existing.getUserId().equals(publication.getUserId())) {
            throw new InvalidPublicationDataException("No se puede cambiar el autor de la publicación.");
        }
        if (publicationRepository.hasPendingProposals(publicationId)) {
            throw new InvalidPublicationDataException("Resuelve las propuestas pendientes antes de modificar la publicación.");
        }
        publication.setPublicationStatus(existing.getPublicationStatus());
        validatePublication(publication);
        return publicationRepository.updatePublication(publicationId, publication);
    }

    @Override
    @Transactional
    public Publication changePublicationStatus(Integer publicationId, String status) {
        validateStatus(status);
        lockPublication(publicationId);
        Publication existing = findPublicationById(publicationId);
        existing.setPublicationStatus(status);
        return publicationRepository.updatePublication(publicationId, existing);
    }

    @Override
    @Transactional
    public void deletePublication(Integer publicationId) {
        lockPublication(publicationId);
        Publication publication = findPublicationById(publicationId);
        if (!PublicationStatus.PUBLISHED.name().equals(publication.getPublicationStatus())) {
            throw new InvalidPublicationDataException("Solo puedes retirar publicaciones disponibles.");
        }
        publicationRepository.rejectPendingProposals(publicationId);
        publication.setPublicationStatus(PublicationStatus.CANCELLED.name());
        publicationRepository.updatePublication(publicationId, publication);
    }

    @Override
    @Transactional
    public void lockPublication(Integer publicationId) {
        publicationRepository.lockPublication(publicationId);
    }

    private void validatePublication(Publication publication) {
        if (isBlank(publication.getTitle())) {
            throw new InvalidPublicationDataException("El título es obligatorio.");
        }

        publication.setTitle(publication.getTitle().trim());
        if (publication.getTitle().length() > 150 || isBlank(publication.getDescription())) {
            throw new InvalidPublicationDataException("Introduce un título válido y una descripción del servicio.");
        }
        publication.setDescription(publication.getDescription().trim());
        if (publication.getDescription().length() > 4000) throw new InvalidPublicationDataException("La descripción admite hasta 4000 caracteres.");
        if (!BusinessRules.validAmount(publication.getPheltAmount())) {
            throw new InvalidPublicationDataException("El valor debe ser positivo, con un máximo de dos decimales.");
        }

        if (publication.getUserId() == null) {
            throw new InvalidPublicationDataException("Selecciona un usuario.");
        }

        if (publication.getCategoryId() == null) {
            throw new InvalidPublicationDataException("Selecciona una categoría.");
        }

        if (!isValidEnumValue(PublicationType.class, publication.getPublicationType())) {
            throw new InvalidPublicationDataException(
                    "Selecciona un tipo de publicación válido.");
        }

        if (!isValidEnumValue(InteractionMode.class, publication.getInteractionMode())) {
            throw new InvalidPublicationDataException(
                    "Selecciona una modalidad válida.");
        }

        if (publication.getDurationMinutes() == null || publication.getDurationMinutes() <= 0) {
            throw new InvalidPublicationDataException("Indica una duración positiva en minutos.");
        }
        validateStatus(publication.getPublicationStatus());
        if (PublicationType.NEEDS_SUPPORT.name().equals(publication.getPublicationType())
                && walletService.findWalletByUserId(publication.getUserId()).getAvailableBalance().compareTo(publication.getPheltAmount()) < 0) {
            throw new com.phelti.phelti.exception.InsufficientBalanceException(publication.getUserId());
        }
    }

    private void validateStatus(String status) {
        if (!isValidEnumValue(PublicationStatus.class, status)) {
            throw new InvalidPublicationDataException(
                    "Selecciona un estado de publicación válido.");
        }
    }

    private String normalizeFilter(String value) {
        return isBlank(value) ? null : value.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <E extends Enum<E>> boolean isValidEnumValue(Class<E> enumClass, String value) {
        return value != null && Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(constant -> constant.name().equals(value));
    }

    private <E extends Enum<E>> String allowedValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
