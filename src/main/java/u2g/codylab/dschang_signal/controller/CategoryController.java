package u2g.codylab.dschang_signal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return new ResponseEntity<>(categoryService.createCategory(categoryRequestApiDTO, email), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        categoryService.deleteCategory(id, email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}