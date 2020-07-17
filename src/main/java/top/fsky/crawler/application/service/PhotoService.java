package top.fsky.crawler.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import top.fsky.crawler.adapter.inbound.payloads.PagedResponse;
import top.fsky.crawler.adapter.inbound.payloads.PhotoRequest;
import top.fsky.crawler.adapter.inbound.payloads.PhotoResponse;
import top.fsky.crawler.application.exception.AppException;
import top.fsky.crawler.application.exception.BadRequestException;
import top.fsky.crawler.application.exception.ResourceNotFoundException;
import top.fsky.crawler.application.model.*;
import top.fsky.crawler.application.repository.PhotoRepository;
import top.fsky.crawler.application.repository.TagRepository;
import top.fsky.crawler.application.utils.AppConstants;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository, TagRepository tagRepository) {
        this.photoRepository = photoRepository;
        this.tagRepository = tagRepository;
    }

    public PagedResponse<PhotoResponse> getAllPhotos(int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<Photo> photos = photoRepository.findAll(pageable);

        List<PhotoResponse> photoResponses = photos.map(
                photo -> new PhotoResponse(
                        photo.getId(),
                        photo.getName(),
                        photo.getPath(),
                        photo.getHost(),
                        photo.getUrl(),
                        photo.getTags())
        ).getContent();
        
        return new PagedResponse<>(photoResponses, photos.getNumber(),
                photos.getSize(), photos.getTotalElements(),
                photos.getTotalPages(), photos.isLast()
        );
    }

    public Photo createPhoto(PhotoRequest photoRequest) {
        Set<Tag> tags = null;
        if (photoRequest.getTags() != null){
            tags = photoRequest.getTags().stream()
                    .map(tagStr -> {
                        TagName tagName = TagName.valueOf(tagStr);
                        return tagRepository.findByName(tagName)
                                .orElseThrow(() -> new AppException("Tag not found."));
                    })
                    .collect(Collectors.toSet());
        }
        if (tags == null){
            Tag tag = tagRepository.findByName(TagName.TAG_UNKNOWN)
                    .orElseThrow(() -> new AppException("User Role not set."));

            tags = Collections.singleton(tag);
        }
        
        Photo photo = Photo.builder()
                .name(photoRequest.getName())
                .host(photoRequest.getHost())
                .path(photoRequest.getPath())
                .url(photoRequest.getUrl())
                .tags(tags)
                .build();

        return photoRepository.save(photo);
    }

    public PhotoResponse getPhotoById(Long photoId) {
        Photo photo = photoRepository.findById(photoId).orElseThrow(
                () -> new ResourceNotFoundException("Product", "id", photoId));

        return new PhotoResponse(
                photo.getId(),
                photo.getName(),
                photo.getPath(),
                photo.getHost(),
                photo.getUrl(),
                photo.getTags());
    }

    private void validatePageNumberAndSize(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if (size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

}
