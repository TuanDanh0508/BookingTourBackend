package com.danh.bookingtour.mapper;

import com.danh.bookingtour.entity.Client;
import com.danh.bookingtour.entity.ClientType;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ClientMapper {

    @Select("<script>" +
            "SELECT * FROM m_client WHERE is_active = true " +
            "<if test='type != null'> AND type = #{type} </if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Client> findAllActive(@Param("type") String type);

    @Select("SELECT * FROM m_client WHERE id = #{id} AND is_active = true")
    Optional<Client> findByIdActive(Long id);

    @Insert("INSERT INTO m_client (name, type, contact_name, phone, email, address, description, is_active) " +
            "VALUES (#{name}, #{type}, #{contactName}, #{phone}, #{email}, #{address}, #{description}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Client client);

    @Update("UPDATE m_client SET name = #{name}, type = #{type}, contact_name = #{contactName}, " +
            "phone = #{phone}, email = #{email}, address = #{address}, description = #{description} " +
            "WHERE id = #{id}")
    int update(Client client);

    @Update("UPDATE m_client SET is_active = false WHERE id = #{id}")
    int softDelete(Long id);
    
    @Select("SELECT COUNT(*) > 0 FROM m_client WHERE name = #{name} AND type = #{type} AND id != COALESCE(#{excludeId}, -1)")
    boolean existsByNameAndType(@Param("name") String name, @Param("type") String type, @Param("excludeId") Long excludeId);
}
