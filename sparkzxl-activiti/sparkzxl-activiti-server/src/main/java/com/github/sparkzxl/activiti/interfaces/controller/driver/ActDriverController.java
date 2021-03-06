package com.github.sparkzxl.activiti.interfaces.controller.driver;

import com.github.sparkzxl.activiti.api.ActivitiDriverApi;
import com.github.sparkzxl.activiti.application.service.driver.IActivitiDriverService;
import com.github.sparkzxl.activiti.dto.ActivitiDataDTO;
import com.github.sparkzxl.activiti.dto.DriverProcessDTO;
import com.github.sparkzxl.activiti.dto.DriverResult;
import com.github.sparkzxl.activiti.dto.UserNextTask;
import com.github.sparkzxl.activiti.interfaces.dto.process.ProcessNextTaskDTO;
import com.github.sparkzxl.log.annotation.WebLog;
import com.github.sparkzxl.core.annotation.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description: 流程驱动管理
 *
 * @author: zhouxinlei
 * @date: 2020-07-17 16:29:54
 */
@AllArgsConstructor
@RestController
@RequestMapping("/act/driver")
@ResponseResult
@WebLog
@Api(tags = "流程驱动管理")
public class ActDriverController implements ActivitiDriverApi {

    private final IActivitiDriverService activitiDriverService;

    @Override
    public DriverResult driverProcess(DriverProcessDTO driverProcessDTO) {
        return activitiDriverService.driverProcess(driverProcessDTO);
    }

    @Override
    public ActivitiDataDTO findActivitiData(String businessId, String processDefinitionKey) {
        return activitiDriverService.findActivitiData(businessId, processDefinitionKey);
    }

    @Override
    public List<ActivitiDataDTO> findActivitiDataList(List<String> businessIds, String processDefinitionKey) {
        return activitiDriverService.findActivitiDataList(businessIds, processDefinitionKey);
    }

    @ApiOperation(value = "获取下一步任务详情")
    @PostMapping("nextUserTask")
    public List<UserNextTask> getNextUserTask(@RequestBody @Validated ProcessNextTaskDTO processNextTaskDTO) {
        return activitiDriverService.getNextUserTask(processNextTaskDTO);
    }

    @Override
    public boolean suspendProcess(String businessId) {
        return activitiDriverService.suspendProcess(businessId);
    }

    @ApiOperation("根据流程实例id挂起流程")
    @DeleteMapping("/suspendProcessByProcessInstanceId")
    boolean suspendProcessByProcessInstanceId(@RequestParam("processInstanceId") String processInstanceId) {
        return activitiDriverService.suspendProcessByProcessInstanceId(processInstanceId);
    }

    @Override
    public boolean deleteProcessInstance(String businessId, String deleteReason) {
        return activitiDriverService.deleteProcessInstance(businessId, deleteReason);
    }

    /**
     * 删除流程
     *
     * @param processInstanceId 业务主键
     * @param deleteReason      删除原因
     * @return boolean
     */
    @ApiOperation("根据流程实例id删除流程实例")
    @DeleteMapping("/deleteProcessByProcInsId")
    boolean deleteProcessByProcInsId(@RequestParam("processInstanceId") String processInstanceId,
                                     @RequestParam("deleteReason") String deleteReason) {
        return activitiDriverService.deleteProcessByProcInsId(processInstanceId, deleteReason);

    }
}
