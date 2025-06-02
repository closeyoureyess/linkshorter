package com.effective.linkshorter.controller.internalcontrollers;

import com.effective.linkshorter.annotations.FilterResponse;
import com.effective.linkshorter.controller.LinkShorterController;
import com.effective.linkshorter.entity.LinkDto;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.List;

import static com.effective.linkshorter.others.ConstantsClass.POST_CREATE_LINK;

@ControllerAdvice(assignableTypes = {LinkShorterController.class})
public class UniversalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(returnType);

        if (resolvableType == ResolvableType.NONE) {
            return false;
        }

        Class<?> rawClass = resolvableType.getRawClass();

        if (rawClass == null || !ResponseEntity.class.isAssignableFrom(rawClass)) {
            return false;
        }

        ResolvableType innerType = resolvableType.getGeneric(0);
        if (innerType == ResolvableType.NONE) {
            return false;
        }

        Class<?> resolvedClass = innerType.resolve();
        if (resolvedClass == null) {
            return false;
        }

        if (isSupportedDto(resolvedClass)) {
            return true;
        }

        return isSupportedCollection(resolvedClass, innerType);
    }

    /**
     * Проверяет, явлется ли классом c тем возвращаемым Dto, который обрабатывается.
     */

    private boolean isSupportedDto(Class<?> clazz) {
        return LinkDto.class.isAssignableFrom(clazz);
    }

    /**
     * Проверяет, является ли класс коллекцией с поддерживаемыми элементами.
     */

    private boolean isSupportedCollection(Class<?> clazz, ResolvableType innerType) {
        if (!Collection.class.isAssignableFrom(clazz)) {
            return false;
        }

        ResolvableType elementType = innerType.getGeneric(0);
        if (elementType == ResolvableType.NONE) {
            return false;
        }

        Class<?> elementClass = elementType.resolve();
        if (elementClass == null) {
            return false;
        }

        return LinkDto.class.isAssignableFrom(elementClass);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String linkDtoFilter = "LinkDtoFilter";

        // Проверяем, является ли тело ответа экземпляром NotesDto
        if (!(body instanceof LinkDto) && !(body instanceof List<?>)) {

            return body;
        }

        // Проверяем, что запрос является ServletServerHttpRequest
        if (!(request instanceof ServletServerHttpRequest)) {
            return body;
        }

        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

        // Получаем HandlerMethod из атрибутов запроса
        Object handler = httpServletRequest.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler");

        if (!(handler instanceof HandlerMethod)) {
            return body;
        }

        //Получить HandlerMethod, который представляет метод контроллера, обрабатывающий текущий запрос.
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Получаем аннотацию @FilterResponse
        FilterResponse filterResponse = handlerMethod.getMethodAnnotation(FilterResponse.class);

        if (filterResponse == null) {
            return body;
        }

        String filterName = filterResponse.filterName();
        MappingJacksonValue mapping = new MappingJacksonValue(body);
        SimpleBeanPropertyFilter linkFilter;

        FilterProvider filters = switch (filterName) {
            case POST_CREATE_LINK -> {
                // Определяем, какие поля включать в LinkDto
                linkFilter = SimpleBeanPropertyFilter.filterOutAllExcept("id", "linkWithUniqueIdentifier");

                // Создаём провайдер фильтров
                filters = new SimpleFilterProvider().addFilter(linkDtoFilter, linkFilter);
                yield filters;
            }
            default -> null;
        };

        if (filters != null) {
            mapping.setFilters(filters);
        }
        return mapping;
    }
}