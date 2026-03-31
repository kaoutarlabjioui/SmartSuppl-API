package org.smartsupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.smartsupply.dto.response.SalesOrderResponseDto;
import org.smartsupply.model.entity.SalesOrder;

@Mapper(componentModel = "spring", uses = { SalesOrderLineMapper.class })
public interface SalesOrderMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(order.getClient()!=null? order.getClient().getFirstName()+\" \"+order.getClient().getLastName(): null)")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", expression = "java(order.getWarehouse()!=null? order.getWarehouse().getName(): null)")
    @Mapping(target = "scheduledShippingDate", expression = "java(calculateScheduledDate(order.getCreatedAt()))")
    SalesOrderResponseDto toResponse(SalesOrder order);

    default java.time.LocalDateTime calculateScheduledDate(java.time.LocalDateTime createdAt) {
        if (createdAt == null)
            return null;
        java.time.LocalDateTime scheduled = createdAt;

        // Cut-off: 15h
        if (createdAt.getHour() >= 15) {
            scheduled = scheduled.plusDays(1);
            // Reset time to start of day or keep same time? Requirement usually implies
            // processing start next day.
            // Let's keep the date part important.
        }

        // Weekend handling (Saturday=6, Sunday=7)
        java.time.DayOfWeek day = scheduled.getDayOfWeek();
        if (day == java.time.DayOfWeek.SATURDAY) {
            scheduled = scheduled.plusDays(2);
        } else if (day == java.time.DayOfWeek.SUNDAY) {
            scheduled = scheduled.plusDays(1);
        }

        return scheduled;
    }
}