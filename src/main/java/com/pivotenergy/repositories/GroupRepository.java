/*
 * ________________________________________________________________________
 * METRO.IO CONFIDENTIAL
 * ________________________________________________________________________
 *
 * Copyright (c) 2017.
 * Metro Labs Incorporated
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Metro Labs Incorporated and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Metro Labs Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Metro Labs Incorporated.
 */

package com.pivotenergy.repositories;

import com.pivotenergy.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface GroupRepository extends PagingAndSortingRepository<Group, String> {
    Optional<Group> findByAccountEmail(String email);


    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    @Override
    @Modifying
    Group save(Group entity);

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT COUNT(o) FROM Group o WHERE o.deleted = false")
    long count();

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Group o WHERE o.deleted = false")
    boolean existsById(String id);

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Override
    @Modifying
    @Query("DELETE FROM Group o WHERE o.id = ?1")
    void deleteById(String id);

    /**
     * Soft delete the entity with the given id by setting the deleted flag to true.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Modifying
    @Transactional(readOnly = true)
    @Query("UPDATE Group o SET o.deleted = true WHERE o.id = ?1")
    void softDeleteById(String id);

    /**
     * Deletes a given entity.
     *
     * @param entity
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    @Modifying
    @PreAuthorize("#entity.group.id == #principal.getTenantId()")
    void delete(Group entity);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM Group o WHERE o.deleted = false")
    Iterable<Group> findAll(Sort sort);


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM Group o WHERE o.deleted = false")
    Iterable<Group> findAll();


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM Group o WHERE o.deleted = false")
    Page<Group> findAll(Pageable pageable);

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM Group o WHERE o.deleted = false AND o.id = ?1")
    Optional<Group> findById(String id);
}