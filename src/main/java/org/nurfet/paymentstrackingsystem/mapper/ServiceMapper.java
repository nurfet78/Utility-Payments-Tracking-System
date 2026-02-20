package org.nurfet.paymentstrackingsystem.mapper;

import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.ServiceInfoDto;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceInfoDto toDto(ServiceType serviceType) {
        return ServiceInfoDto.builder()
                .code(serviceType.name())
                .displayName(serviceType.getDisplayName())
                .hasMeter(serviceType.isHasMeter())
                .meterDigits(serviceType.getMeterDigits())
                .build();
    }
}
