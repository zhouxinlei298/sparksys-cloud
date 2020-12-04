package com.github.sparkzxl.activiti.infrastructure.strategy;

import com.github.sparkzxl.activiti.application.service.act.IProcessRuntimeService;
import com.github.sparkzxl.activiti.domain.model.DriveProcess;
import com.github.sparkzxl.activiti.domain.model.DriverData;
import com.github.sparkzxl.activiti.domain.service.act.ActWorkApiService;
import com.github.sparkzxl.activiti.dto.DriverResult;
import com.github.sparkzxl.activiti.infrastructure.constant.WorkflowConstants;
import com.github.sparkzxl.core.support.SparkZxlExceptionAssert;
import com.github.sparkzxl.redisson.lock.RedisDistributedLock;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * description: 推动activiti流程
 *
 * @author: zhouxinlei
 * @date: 2020-07-20 16:28:09
 */
@Component
@Slf4j
public class ActivitiSubmitProcessSolver extends AbstractActivitiSolver {

    @Autowired
    private IProcessRuntimeService processRuntimeService;
    @Autowired
    private ActWorkApiService actWorkApiService;
    @Autowired
    private RedisDistributedLock redisDistributedLock;

    @Override
    public DriverResult slove(DriveProcess driveProcess) {
        String businessId = driveProcess.getBusinessId();
        String applyUserId = driveProcess.getApplyUserId();
        String userId = driveProcess.getUserId();
        DriverResult driverResult = new DriverResult();
        boolean lock = redisDistributedLock.lock(businessId,0,15);
        if (lock){
            Map<String, Object> variables = Maps.newHashMap();
            if (StringUtils.isNotEmpty(applyUserId)) {
                variables.put("assignee", applyUserId);
            }
            variables.put("actType", driveProcess.getActType());
            ProcessInstance processInstance = processRuntimeService.getProcessInstanceByBusinessId(businessId);
            if (ObjectUtils.isEmpty(processInstance)){
                redisDistributedLock.releaseLock(businessId);
                SparkZxlExceptionAssert.businessFail("流程实例为空，请检查参数是否正确");
            }
            DriverData driverData = DriverData.builder()
                    .userId(userId)
                    .processInstanceId(processInstance.getProcessInstanceId())
                    .businessId(businessId)
                    .processDefinitionKey(processInstance.getProcessDefinitionKey())
                    .actType(driveProcess.getActType())
                    .comment(driveProcess.getComment())
                    .variables(variables)
                    .build();

            driverResult = actWorkApiService.promoteProcess(driverData);
            redisDistributedLock.releaseLock(businessId);
        }
        return driverResult;
    }

    @Override
    public Integer[] supports() {
        return new Integer[]{WorkflowConstants.WorkflowAction.SUBMIT,
                WorkflowConstants.WorkflowAction.AGREE,
                WorkflowConstants.WorkflowAction.END};
    }
}
