package u2g.codylab.dschang_signal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.api.CategoryApi;
import u2g.codylab.dschang_signal.dto.CategoryRequestApiDTO;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.service.CategoryService;

@RestController
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ResponseEntity<CategoryResponseApiDTO> createCategory(CategoryRequestApiDTO categoryRequestApiDTO) {
        return new ResponseEntity<>(categoryService.createCategory(categoryRequestApiDTO), HttpStatus.CREATED);
    }
}
