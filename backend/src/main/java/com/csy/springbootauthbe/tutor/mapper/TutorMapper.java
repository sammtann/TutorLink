package com.csy.springbootauthbe.tutor.mapper;

import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.entity.QualificationFile;
import com.csy.springbootauthbe.tutor.entity.Tutor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TutorMapper {

    @Mapping(source = "profileImageUrl", target = "profileImageUrl")
    @Mapping(target = "qualifications", expression = "java(filterQualifications(tutor.getQualifications()))")
    @Mapping(target = "reviews", expression = "java(safeList(tutor.getReviews()))")
    @Mapping(target = "availability", expression = "java(safeMap(tutor.getAvailability()))")
    @Mapping(target = "lessonType", expression = "java(safeList(tutor.getLessonType()))")
    TutorDTO toDTO(Tutor tutor);

    default <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    default <K, V> Map<K, V> safeMap(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }

    default List<QualificationFile> filterQualifications(List<QualificationFile> qualifications) {
        return qualifications == null ? new ArrayList<>() :
                qualifications.stream()
                        .filter(q -> !q.isDeleted())
                        .collect(Collectors.toList());
    }

    Tutor toEntity(TutorDTO tutorDTO);
}
