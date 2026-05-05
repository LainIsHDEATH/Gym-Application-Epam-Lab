package ua.ivan.epam.gym.application.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepo<ID, T> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entity);

    void deleteById(ID id);
}