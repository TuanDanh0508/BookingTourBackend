package com.danh.bookingtour.mapper;

import com.danh.bookingtour.entity.Client;
import com.danh.bookingtour.entity.ClientType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ClientMapper {

    List<Client> findAllActive(@Param("type") String type);

    Optional<Client> findByIdActive(Long id);

    int insert(Client client);

    int update(Client client);

    int softDelete(Long id);
    
    boolean existsByNameAndType(@Param("name") String name, @Param("type") String type, @Param("excludeId") Long excludeId);
}
