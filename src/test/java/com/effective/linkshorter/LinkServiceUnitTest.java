package com.effective.linkshorter;

import com.effective.linkshorter.entity.Link;
import com.effective.linkshorter.entity.LinkDto;
import com.effective.linkshorter.mapper.LinkMapper;
import com.effective.linkshorter.repository.LinkRepository;
import com.effective.linkshorter.service.CacheService;
import com.effective.linkshorter.service.LinkServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceUnitTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkMapper linkMapper;

    @Mock
    private CacheService cacheService;

    @Mock
    private HttpServletRequest request; // Можно подставить заглушку

    @InjectMocks
    private LinkServiceImpl linkService;

    // Чтобы гарантировать повторяемость рандомной части,
    // можем подменить SecureRandom через рефлексию или оставить как есть,
    // если не нужно жёстко фиксировать результат.
    // В данном примере проверим только структуру, а не конкретный идентификатор.

    private LinkDto inputDto;

    @BeforeEach
    void setup() {
        // Подготовим простой DTO с одним единственным полем sourceLink
        inputDto = new LinkDto();
        inputDto.setSourceLink("https://example.com/test");
        // остальные поля null по умолчанию (id, uniqueIdentifier, linkWithUniqueIdentifier)
    }

    @Test
    void testLinkCutter_whenNewLink_thenSaveAndReturnDto() {
        // --- 1. Подготовка входных данных ---
        // 1.1: чистим ожидаемое поле uniqueIdentifier (оно null)
        // 1.2: модель Link (сгенерируем вручную) и LinkDto, которые вернёт маппер
        String fakeUniqueId = "ABC123";
        String fakeShortLink = "http://localhost:8080/" + fakeUniqueId;

        // Подменим логику SecureRandom (можно засетить заранее в StringBuilder),
        // но для простоты: когда service вызывает findByUniqueIdentifier, вернём пустое.
        when(cacheService.getFromCache(eq("linkServiceCache"), anyString()))
                .thenReturn(Optional.empty());
        when(linkRepository.findByUniqueIdentifier(anyString()))
                .thenReturn(Optional.empty());

        // Когда маппер получит DTO (с уже заданным uniqueIdentifier),
        // должен вернуть сущность Link для сохранения:
        ArgumentCaptor<Link> savedLinkCaptor = ArgumentCaptor.forClass(Link.class);
        // Предскажем, что save вернёт Link с id=1, sourceLink взят из inputDto, uniqueIdentifier=fakeUniqueId
        Link savedLink = new Link();
        savedLink.setId(1L);
        savedLink.setSourceLink(inputDto.getSourceLink());
        savedLink.setUniqueIdentifier(fakeUniqueId);
        savedLink.setLinkWithUniqueIdentifier(fakeShortLink);
        when(linkRepository.save(any(Link.class))).thenReturn(savedLink);

        // Когда конвертируем сохранённую сущность обратно в DTO:
        LinkDto returnDto = new LinkDto();
        returnDto.setId(1L);
        returnDto.setSourceLink(inputDto.getSourceLink());
        returnDto.setUniqueIdentifier(fakeUniqueId);
        returnDto.setLinkWithUniqueIdentifier(fakeShortLink);
        when(linkMapper.convertLinkToDto(savedLink)).thenReturn(returnDto);

        // Захватываем момент, когда convertDtoToLink должен выполнить обратную конвертацию:
        when(linkMapper.convertDtoToLink(any(LinkDto.class)))
                .thenAnswer(invocation -> {
                    // Возвращаем любую новую сущность на основе переданного DTO
                    Link tmp = new Link();
                    tmp.setSourceLink(((LinkDto) invocation.getArgument(0)).getSourceLink());
                    tmp.setUniqueIdentifier(((LinkDto) invocation.getArgument(0)).getUniqueIdentifier());
                    tmp.setLinkWithUniqueIdentifier(((LinkDto) invocation.getArgument(0)).getLinkWithUniqueIdentifier());
                    return tmp;
                });

        // --- 2. Вызов тестируемого метода ---
        Optional<LinkDto> resultOpt = linkService.linkCutter(inputDto, request);

        // --- 3. Проверка результатов ---
        assertTrue(resultOpt.isPresent(), "Ожидаем, что метод вернёт непустой Optional");
        LinkDto result = resultOpt.get();
        assertEquals(1L, result.getId(), "Id должен быть 1L");
        assertEquals(inputDto.getSourceLink(), result.getSourceLink(), "Исходная ссылка должна совпадать");
        assertEquals(fakeUniqueId, result.getUniqueIdentifier(), "Уникальный идентификатор должен соответствовать заглушке");
        assertEquals(fakeShortLink, result.getLinkWithUniqueIdentifier(), "Сокращённая ссылка должна включать localhost и идентификатор");

        // Убедимся, что репозиторий save вызывался ровно один раз
        verify(linkRepository, times(1)).save(savedLinkCaptor.capture());
        Link linkPassedToSave = savedLinkCaptor.getValue();
        assertEquals(inputDto.getSourceLink(), linkPassedToSave.getSourceLink(), "Перед сохранением sourceLink должен быть тот же");
    }

    @Test
    void testGetSourceLink_whenExistsInDb_thenReturnDto() {
        // --- 1. Подготовка ---
        String uniqueId = "XYZ789";
        Link entity = new Link();
        entity.setId(2L);
        entity.setSourceLink("https://somewhere.org/page");
        entity.setUniqueIdentifier(uniqueId);
        entity.setLinkWithUniqueIdentifier("http://localhost:8080/" + uniqueId);

        // Мокаем репозиторий, чтобы findByUniqueIdentifier вернул нашу сущность
        when(linkRepository.findByUniqueIdentifier(uniqueId)).thenReturn(Optional.of(entity));

        // Маппер конвертирует сущность в DTO
        LinkDto dto = new LinkDto(2L, entity.getSourceLink(), uniqueId, entity.getLinkWithUniqueIdentifier());
        when(linkMapper.convertLinkToDto(entity)).thenReturn(dto);

        // --- 2. Вызов ---
        Optional<LinkDto> resultOpt = linkService.getSourceLink(uniqueId);

        // --- 3. Проверка ---
        assertTrue(resultOpt.isPresent(), "Ожидаем, что DTO найдено в БД");
        LinkDto result = resultOpt.get();
        assertEquals(2L, result.getId());
        assertEquals(entity.getSourceLink(), result.getSourceLink());
        assertEquals(uniqueId, result.getUniqueIdentifier());
        assertEquals(entity.getLinkWithUniqueIdentifier(), result.getLinkWithUniqueIdentifier());

        // Проверим, что репозиторий вызывался именно с этим уникальным идентификатором
        verify(linkRepository, times(1)).findByUniqueIdentifier(uniqueId);
    }

    @Test
    void testGetSourceLink_whenNotExists_thenEmpty() {
        // --- 1. Подготовка ---
        String notExistingId = "NOPE000";
        when(linkRepository.findByUniqueIdentifier(notExistingId)).thenReturn(Optional.empty());

        // --- 2. Вызов ---
        Optional<LinkDto> resultOpt = linkService.getSourceLink(notExistingId);

        // --- 3. Проверка ---
        assertFalse(resultOpt.isPresent(), "Ожидаем, что Optional пустой, т.к. записи нет");
        verify(linkRepository, times(1)).findByUniqueIdentifier(notExistingId);
    }
}
