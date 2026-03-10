package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import u2g.codylab.dschang_signal.dto.CategoryRequestApiDTO;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Category;
import u2g.codylab.dschang_signal.mapper.CategoryMapper;
import u2g.codylab.dschang_signal.repository.CategoryRepository;

@Slf4j
@Transactional
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponseApiDTO createCategory(CategoryRequestApiDTO categoryRequestApiDTO) {
        log.debug("Creating category: {}", categoryRequestApiDTO.getName());
        Category category = categoryMapper.toEntity(categoryRequestApiDTO);
        CategoryResponseApiDTO categoryDTO = categoryMapper.toCategoryDto(categoryRepository.save(category));
        log.debug("Category created: {}", categoryDTO.getName());
        return categoryDTO;
    }
}
