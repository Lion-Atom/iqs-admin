package me.zhengjie.service;

import me.zhengjie.domain.Instrument;
import me.zhengjie.service.dto.InstrumentDto;
import me.zhengjie.service.dto.InstrumentQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/27 13:32
 */
public interface InstrumentService {

    List<InstrumentDto> queryAll(InstrumentQueryCriteria criteria);

    void download(List<InstrumentDto> queryAll, HttpServletResponse response) throws IOException;

    Map<String, Object> queryByPage(InstrumentQueryCriteria criteria, Pageable pageable);

    Instrument findById(Long id);

    void create(InstrumentDto resource);

    void update(Instrument resource);

    void delete(Set<Long> ids);
}
