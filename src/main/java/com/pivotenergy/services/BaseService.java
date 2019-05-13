package com.pivotenergy.services;

import com.pivotenergy.domain.BaseDomainEntity;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.exceptions.PivotInvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public abstract class BaseService<T extends BaseDomainEntity, R extends PagingAndSortingRepository> {
    @SuppressWarnings("WeakerAccess")
    protected Logger LOG = LoggerFactory.getLogger(GroupService.class);
    @SuppressWarnings("WeakerAccess")
    protected R repository;
    private Class<T> clazz;

    BaseService(Class<T> clazz, R repository) {
        this.clazz = clazz;
        this.repository = repository;
    }

    public R getRepository() {
        return repository;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public T getById(String id) throws Throwable {
        return clazz.cast(repository.findById(id).orElseThrow(new PivotEntityNotFoundException(clazz, id)));
    }

    @Transactional
    public T create(T entity) {
        return clazz.cast(repository.save(entity));
    }

    @Transactional
    public T update(String id, T update) throws Throwable {
        T entity = getById(id);

        if(entity.getId().equals(update.getId())) {
            return clazz.cast(repository.save(update));
        }

        String message = String.format("Invalid group identifiers! Attempting to update Group with id = %s " +
                "but the provided path variable id is %s", update.getId(), id);
        throw new PivotInvalidRequestException("Invalid Identifiers Provided", message);
    }

    @SuppressWarnings("unused")
    abstract T patch(String id, Map<String, Object> patch) throws Throwable;

    @SuppressWarnings("unused")
    abstract void softDelete(String id);

    @SuppressWarnings("unused")
    abstract void hardDelete(String id);
}
