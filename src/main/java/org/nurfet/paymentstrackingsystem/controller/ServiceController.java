package org.nurfet.paymentstrackingsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.nurfet.paymentstrackingsystem.domain.ServiceType;
import org.nurfet.paymentstrackingsystem.dto.ServiceInfoDto;
import org.nurfet.paymentstrackingsystem.mapper.ServiceMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Справочник коммунальных услуг.
 * Набор из 8 услуг фиксирован и определён в enum ServiceType.
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Tag(name = "Услуги", description = "Справочник коммунальных услуг (фиксированный набор из 8 типов)")
public class ServiceController {

    private final ServiceMapper serviceMapper;

    @GetMapping
    @Operation(
            summary = "Получить список всех услуг",
            description = """
                    Возвращает полный справочник коммунальных услуг.
                    Набор фиксирован: Газ, Вода, Электроэнергия, Домофон,
                    Отопление, Экоресурсы, Жилсервис, Капитальный ремонт.
                    Для каждой услуги указано наличие счётчика и количество цифр в показании."""
    )
    @ApiResponse(responseCode = "200", description = "Список услуг успешно получен")
    public List<ServiceInfoDto> getAll() {
        return Arrays.stream(ServiceType.values())
                .map(serviceMapper::toDto)
                .toList();
    }
}
