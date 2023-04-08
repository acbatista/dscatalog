package com.estudos.spring.backend.services;

import com.estudos.spring.backend.dto.CategoryDTO;
import com.estudos.spring.backend.dto.ProductDTO;
import com.estudos.spring.backend.entities.Category;
import com.estudos.spring.backend.entities.Product;
import com.estudos.spring.backend.repositories.CategoryRepository;
import com.estudos.spring.backend.repositories.ProductRepository;
import com.estudos.spring.backend.repositories.ProductRepository;
import com.estudos.spring.backend.services.exceptions.DataBaseException;
import com.estudos.spring.backend.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
        Page<Product> list = repository.findAll(pageRequest);
        return list.map(x -> new ProductDTO(x));

    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);

        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity nof found"));
        return new ProductDTO(entity,entity.getCategoies());


    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);

        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {

        try {
            Product entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new ProductDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }

    }

    public void delete(Long id) {

        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);

        } catch (DataIntegrityViolationException e) {
            throw  new DataBaseException("Integrity violation");
        }

    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {

        entity.setName(dto.getName());
        entity.setDate(dto.getDate());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());

        entity.getCategoies().clear();
        for (CategoryDTO catDto : dto.getCategories()){
            Category category = categoryRepository.getReferenceById(catDto.getId());

            entity.getCategoies().add(category);
        }


    }
}
