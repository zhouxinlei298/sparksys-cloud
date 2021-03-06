package com.github.sparkzxl.activiti.application.service.ext;

import com.github.pagehelper.PageInfo;
import com.github.sparkzxl.activiti.domain.vo.InstanceOverview;
import com.github.sparkzxl.activiti.infrastructure.entity.ProcessInstance;
import com.github.sparkzxl.activiti.infrastructure.entity.ExtProcessStatus;
import com.github.sparkzxl.activiti.interfaces.dto.act.InstancePageDTO;
import com.github.sparkzxl.database.base.service.SuperCacheService;

import java.util.List;

/**
 * description: 流程状态记录 服务类
 *
 * @author: zhouxinlei
 * @date: 2020-07-17 13:21:47
 */
public interface IExtProcessStatusService extends SuperCacheService<ExtProcessStatus> {

    /**
     * 获取流程状态
     *
     * @param businessId 业务主键
     * @return ProcessTaskStatus
     */
    List<ExtProcessStatus> getExtProcessStatusList(String businessId);


    /**
     * 查询流程状态
     *
     * @param businessId        业务主键
     * @param processInstanceId 流程实例id
     * @return ExtProcessStatus
     */
    ExtProcessStatus getExtProcessStatus(String businessId, String processInstanceId);

    /**
     * 查询流程实例列表
     *
     * @param instancePageDTO 流程实例查询入参
     * @return PageInfo<ProcessInstanceDTO>
     */
    PageInfo<ProcessInstance> getProcessInstanceList(InstancePageDTO instancePageDTO);

    /**
     * 流程统计总览
     *
     * @return InstanceOverview
     */
    InstanceOverview instanceOverview();

}
