package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
    <T> List<T> findAllBy(Class<T> type);
}
