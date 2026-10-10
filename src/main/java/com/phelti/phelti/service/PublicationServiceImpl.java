package com.phelti.phelti.service;

import com.phelti.phelti.exception.InvalidPublicationDataException;
import com.phelti.phelti.exception.PublicationNotFoundException;
import com.phelti.phelti.model.InteractionMode;
import com.phelti.phelti.model.Publication;
import com.phelti.phelti.model.PublicationStatus;
import com.phelti.phelti.model.PublicationType;
import com.phelti.phelti.repository.PublicationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;

    public PublicationServiceImpl(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public Publication createPublication(Publication publication) {
        if (isBlank(publication.getPublicationStatus())) {
            publication.setPublicationStatus(PublicationStatus.PUBLISHED.name());
        }
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
    public Publication updatePublication(Integer publicationId, Publication publication) {
        Publication existing = findPublicationById(publicationId);
        if (isBlank(publication.getPublicationStatus())) {
            publication.setPublicationStatus(existing.getPublicationStatus());
        }
        validatePublication(publication);
        return publicationRepository.updatePublication(publicationId, publication);
    }

    @Override
    public Publication changePublicationStatus(Integer publicationId, String status) {
        Publication existing = findPublicationById(publicationId);
        existing.setPublicationStatus(status);
        return publicationRepository.updatePublication(publicationId, existing);
    }

    @Override
    public void deletePublication(Integer publicationId) {
        findPublicationById(publicationId);
        publicationRepository.deletePublication(publicationId);
    }

    private void validatePublication(Publication publication) {
        if (isBlank(publication.getTitle())) {
            throw new InvalidPublicationDataException("title must not be empty");
        }

        if (publication.getPheltAmount() == null
                || publication.getPheltAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPublicationDataException("pheltAmount must be greater than zero");
        }

        if (publication.getUserId() == null) {
            throw new InvalidPublicationDataException("userId is required");
        }

        if (publication.getCategoryId() == null) {
            throw new InvalidPublicationDataException("categoryId is required");
        }

        if (!isValidEnumValue(PublicationType.class, publication.getPublicationType())) {
            throw new InvalidPublicationDataException(
                    "publicationType must be one of: " + allowedValues(PublicationType.class));
        }

        if (!isValidEnumValue(InteractionMode.class, publication.getInteractionMode())) {
            throw new InvalidPublicationDataException(
                    "interactionMode must be one of: " + allowedValues(InteractionMode.class));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private <E extends Enum<E>> boolean isValidEnumValue(Class<E> enumClass, String value) {
        if (value == null) {
            return false;
        }
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private <E extends Enum<E>> String allowedValues(Class<E> enumClass) {
        StringBuilder sb = new StringBuilder();
        E[] constants = enumClass.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            sb.append(constants[i].name());
            if (i < constants.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
