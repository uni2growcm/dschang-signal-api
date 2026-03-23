package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import u2g.codylab.dschang_signal.dto.CategoryRequestApiDTO;
import u2g.codylab.dschang_signal.dto.CategoryResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Category;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.ConflictException;
import u2g.codylab.dschang_signal.exception.ForbiddenException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.CategoryMapper;
import u2g.codylab.dschang_signal.repository.CategoryRepository;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.util.List;

@Slf4j
@Transactional
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;
    private final I18nService i18nService;

    public CategoryService(CategoryRepository categoryRepository,
                           CategoryMapper categoryMapper,
                           UserRepository userRepository,
                           I18nService i18nService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.userRepository = userRepository;
        this.i18nService = i18nService;
    }

    public CategoryResponseApiDTO createCategory(CategoryRequestApiDTO categoryRequestApiDTO, String email) {
        log.debug("Creating category: {}", categoryRequestApiDTO.getName());

        if (categoryRepository.findByName(categoryRequestApiDTO.getName()).isPresent())
            throw new ConflictException(
                    i18nService.get("category.error.exists", categoryRequestApiDTO.getName())
            );

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("category.error.userNotFound", email)
                ));

        Category category = categoryMapper.toEntity(categoryRequestApiDTO);
        category.setCreatedBy(creator);

        try {
            CategoryResponseApiDTO categoryDTO = categoryMapper.toCategoryDto(categoryRepository.save(category));
            log.debug("Category created: {}", categoryDTO.getName());
            return categoryDTO;
        } catch (Exception e) {
            log.error("Error while creating category: {}", categoryRequestApiDTO.getName(), e);
            throw new BadRequestException(i18nService.get("category.error.createFailed"));
        }
    }

    public List<CategoryResponseApiDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    public void deleteCategory(Long id, String userEmail) {
        log.debug("Deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(i18nService.get("category.error.notFound", id)));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("category.error.userNotFound", userEmail)
                ));

        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isCreator = category.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreator) {
            throw new ForbiddenException(i18nService.get("category.error.forbidden"));
        }

        categoryRepository.delete(category);
        log.debug("Category with id {} deleted", id);
    }
}