package org.smartsupply.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.smartsupply.dto.request.ProductRequestDto;
import org.smartsupply.dto.request.ProductUpdateDto;
import org.smartsupply.dto.response.ProductResponseDto;
import org.smartsupply.dto.response.ProductSummaryDto;
import org.smartsupply.model.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "profite", source = "profit")
    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "mapMultipartFiles")
    Product toEntity(ProductRequestDto productRequestDto);

    @Mapping(target = "category", source = "category")

    @Mapping(target = "profit", source = "profite")
    @Mapping(target = "imageUrls", source = "imageUrls")

    @Mapping(target = "sellingPrice", expression = "java(calculateSellingPrice(product))")
    ProductResponseDto toResponseDto(Product product);

    @Mapping(target = "profit", source = "profite")
    @Mapping(target = "sellingPrice", expression = "java(calculateSellingPrice(product))")
    ProductSummaryDto toSummaryDto(Product product);

    List<ProductResponseDto> toResponseDtoList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
 
    @Mapping(target = "profite", source = "profit")
    @Mapping(target = "imageUrls", source = "imageUrls", qualifiedByName = "mapMultipartFiles")
    void updateEntityFromDto(ProductUpdateDto productUpdateDto, @MappingTarget Product product);

    default BigDecimal calculateSellingPrice(Product product) {
        if (product.getOriginalPrice() == null || product.getProfite() == null) {
            return BigDecimal.ZERO;
        }
        return product.getOriginalPrice().add(product.getProfite());
    }

    @Named("mapMultipartFiles")
    default List<String> mapMultipartFiles(List<MultipartFile> files) {
        if (files == null) return null;
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());
    }
}
