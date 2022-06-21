package com.yas.product.controller;

import com.yas.product.exception.BadRequestException;
import com.yas.product.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("categories")
    public List<CategoryGetVm> list(){
        return categoryRepository.findAll().stream()
                .map(item -> CategoryGetVm.fromModel(item))
                .collect(Collectors.toList());
    }

    @GetMapping("categories/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = CategoryGetDetailVm.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorVm.class)))})
    public ResponseEntity<CategoryGetDetailVm> get(@PathVariable Long id){
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category %s is not found", id)));

        CategoryGetDetailVm categoryGetDetailVm = CategoryGetDetailVm.fromModel(category);
        return  ResponseEntity.ok(categoryGetDetailVm);
    }

    @PostMapping("categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = @Content(schema = @Schema(implementation = CategoryGetDetailVm.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorVm.class)))})
    public ResponseEntity<CategoryGetDetailVm> create(@Valid @RequestBody CategoryPostVm categoryPostVm){
        Category category = new Category();
        category.setName(categoryPostVm.name());
        category.setSlug(categoryPostVm.slug());
        category.setDescription(categoryPostVm.description());

        if(categoryPostVm.parentId() != null){
            Category parentCategory = categoryRepository
                    .findById(categoryPostVm.parentId())
                    .orElseThrow(() -> new BadRequestException(String.format("Parent category %s is not found", categoryPostVm.parentId())));
            category.setParent(parentCategory);
        }
        categoryRepository.saveAndFlush(category);

        CategoryGetDetailVm categoryGetDetailVm = CategoryGetDetailVm.fromModel(category);
        return  ResponseEntity.ok(categoryGetDetailVm);
    }

    @PutMapping("categories/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorVm.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorVm.class)))})
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid final CategoryPostVm categoryPostVm){
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Category %s is not found", id)));

        category.setName(categoryPostVm.name());
        category.setSlug(categoryPostVm.slug());
        category.setDescription(categoryPostVm.description());
        if(categoryPostVm.parentId() == null){
            category.setParent(null);
        } else {
            Category parentCategory = categoryRepository
                    .findById(categoryPostVm.parentId())
                    .orElseThrow(() -> new BadRequestException(String.format("Parent category %s is not found", categoryPostVm.parentId())));
            category.setParent(parentCategory);
        }

        categoryRepository.saveAndFlush(category);
        return ResponseEntity.noContent().build();
    }
}