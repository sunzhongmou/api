package top.fsky.crawler.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import top.fsky.crawler.application.model.Photo;

import java.util.Optional;

@Repository
public interface PhotoRepository 
        extends JpaRepository<Photo, Long>, JpaSpecificationExecutor<Photo> {
    @Override
    Optional<Photo> findById(Long productId);
}
