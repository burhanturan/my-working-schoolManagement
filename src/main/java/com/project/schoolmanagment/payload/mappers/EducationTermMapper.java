package com.project.schoolmanagment.payload.mappers;

import com.project.schoolmanagment.entity.conceretes.business.EducationTerm;
import com.project.schoolmanagment.payload.request.business.EducationTermRequest;
import com.project.schoolmanagment.payload.response.business.EducationTermResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class EducationTermMapper {

    //DTO -->> to Domain Object Mapper
    public EducationTerm mapEducationTermRequestToEducationTerm(EducationTermRequest educationTermRequest) {
        return EducationTerm.builder()
                .term(educationTermRequest.getTerm())
                .startDate(educationTermRequest.getStartDate())
                .endDate(educationTermRequest.getEndDate())
                .lastRegistrationDate(educationTermRequest.getLastRegistrationDate())
                .build();
    }

    //Domain Object -->> to DTO Mapper
    public EducationTermResponse mapEducationTermToEducationTermResponse(EducationTerm educationTerm) {
        return EducationTermResponse.builder()
                .id(educationTerm.getId())
                .term(educationTerm.getTerm())
                .startDate(educationTerm.getStartDate())
                .endDate(educationTerm.getEndDate())
                .lastRegistrationDate(educationTerm.getLastRegistrationDate())
                .build();
    }

    public EducationTerm mapEducationTermRequestToEducationTermForUpdate(Long id, EducationTermRequest educationTermRequest) {

        return mapEducationTermRequestToEducationTerm(educationTermRequest)
                .toBuilder()
                .id(id)
                .build();

//        EducationTerm term =  mapEducationTermRequestToEducationTerm(educationTermRequest);
//        term.setId(id);
//        return term;

    }


}
