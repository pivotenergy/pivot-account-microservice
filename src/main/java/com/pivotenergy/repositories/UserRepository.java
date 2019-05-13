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

import com.pivotenergy.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT COUNT(o) FROM User o WHERE o.deleted = false AND o.groupId = ?#{principal.getTenantId()}")
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
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM User o " +
            "WHERE o.deleted = false AND o.groupId = ?#{principal.getTenantId()}")
    boolean existsById(String id);

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Override
    @Modifying
    @Query("DELETE FROM User o WHERE o.id = ?1 AND o.groupId = ?#{principal.getTenantId()}")
    void deleteById(String id);

    /**
     * Soft delete the entity with the given id by setting the deleted flag to true.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Modifying
    @Transactional(readOnly = true)
    @Query("UPDATE User o SET o.deleted = true WHERE o.id = ?1 AND o.groupId = ?#{principal.getTenantId()}")
    void softDeleteById(String id);

    /**
     * Deletes all entities managed by the repository.
     */
    @Override
    @Modifying
    @Query("DELETE FROM User o WHERE o.groupId = ?#{principal.getTenantId()}")
    void deleteAll();


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM User o WHERE o.deleted = false AND o.groupId  = ?#{principal.getTenantId()}")
    Iterable<User> findAll(Sort sort);


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM User o WHERE o.deleted = false AND o.groupId  = ?#{principal.getTenantId()}")
    Iterable<User> findAll();


    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM User o WHERE o.deleted = false AND o.groupId = ?#{principal.getTenantId()}")
    Page<User> findAll(Pageable pageable);

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    @Override
    @Transactional(readOnly = true)
    @Query("SELECT o FROM User o WHERE o.deleted = false AND o.id = ?1 AND o.groupId = ?#{principal.getTenantId()}")
    Optional<User> findById(String id);


    @Transactional(readOnly = true)
    Optional<User> findByUserEmail(String email);
}
