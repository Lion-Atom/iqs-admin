package me.zhengjie.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.domain.TrainParticipant;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.service.TrParticipantService;
import me.zhengjie.service.dto.ParticipantQueryByExample;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/24 10:09
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "工具：培训日程参与者信息")
@RequestMapping("/api/trParticipant")
public class TrParticipantController {

    private final TrParticipantService trParticipantService;
    private static final String ENTITY_NAME = "TrainParticipant";

    @ApiOperation("查询培训计划参与者信息")
    @GetMapping(value = "/byTrScheduleId")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getByTrScheduleId(@RequestParam("trScheduleId") Long trScheduleId) {
        return new ResponseEntity<>(trParticipantService.getByTrScheduleId(trScheduleId), HttpStatus.OK);
    }

    @ApiOperation("查询培训计划制定部门已有参与者信息")
    @PostMapping(value = "/byExample")
    @PreAuthorize("@el.check('train:list')")
    public ResponseEntity<Object> getByExample(@RequestBody ParticipantQueryByExample example) {
        return new ResponseEntity<>(trParticipantService.getByExample(example), HttpStatus.OK);
    }

    @Log("新增培训计划参与者信息")
    @ApiOperation("新增培训计划参与者信息")
    @PostMapping
    @PreAuthorize("@el.check('schedule:edit')")
    public ResponseEntity<Object> create(@Validated @RequestBody TrainParticipant resource) {
        if (resource.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        trParticipantService.create(resource);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("新增培训计划参与者信息")
    @ApiOperation("新增培训计划参与者信息")
    @PostMapping("/batchSave")
    @PreAuthorize("@el.check('schedule:edit')")
    public ResponseEntity<Object> batchSave(@Validated @RequestBody List<TrainParticipant> resources) {
        resources.forEach(part -> {
            if (part.getId() != null) {
                throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
            }
        });
        trParticipantService.batchSave(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改培训计划参与者信息")
    @ApiOperation("修改培训计划参与者信息")
    @PutMapping
    @PreAuthorize("@el.check('schedule:edit')")
    public ResponseEntity<Object> update(@Validated(TrainParticipant.Update.class) @RequestBody TrainParticipant resource) {
        trParticipantService.update(resource);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除培训计划参与者信息")
    @ApiOperation("删除培训计划参与者信息")
    @DeleteMapping
    @PreAuthorize("@el.check('schedule:edit')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        trParticipantService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
