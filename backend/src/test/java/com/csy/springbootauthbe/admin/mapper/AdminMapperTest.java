package com.csy.springbootauthbe.admin.mapper;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.entity.Admin;
import com.csy.springbootauthbe.admin.entity.Permissions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdminMapperTest {

    private final AdminMapper mapper = Mappers.getMapper(AdminMapper.class);

    @Test
    void testEntityToDtoAndBack() {
        Admin admin = Admin.builder()
            .id("1")
            .userId("u1")
            .permissions(List.of(Permissions.SUPER_ADMIN))
            .build();

        AdminDTO dto = mapper.toDTO(admin);
        assertEquals(admin.getUserId(), dto.getUserId());

        Admin entity = mapper.toEntity(dto);
        assertEquals(dto.getUserId(), entity.getUserId());
    }
}
