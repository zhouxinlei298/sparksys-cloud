package com.github.sparkzxl.activiti.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.sparkzxl.activiti.domain.repository.IExtProcessDetailRepository;
import com.github.sparkzxl.activiti.infrastructure.entity.ExtProcessDetail;
import com.github.sparkzxl.activiti.infrastructure.mapper.ExtProcessDetailMapper;
import com.github.sparkzxl.database.utils.PageInfoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * description:流程详细节点 仓储实现类
 *
 * @author: zhouxinlei
 * @date: 2020-07-21 15:43:03
 */
@Repository
public class ExtProcessDetailRepositoryImpl implements IExtProcessDetailRepository {

    @Autowired
    private ExtProcessDetailMapper processDetailMapper;

    @Override
    public PageInfo<ExtProcessDetail> getProcessDetailList(int pageNum, int pageSize, String processName) {
        LambdaQueryWrapper<ExtProcessDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(processName).ifPresent((value) -> detailQueryWrapper.eq(ExtProcessDetail::getProcessName, processName));
        detailQueryWrapper.groupBy(ExtProcessDetail::getModelId);
        PageHelper.startPage(pageNum, pageSize);
        return PageInfoUtils.pageInfo(processDetailMapper.selectList(detailQueryWrapper));
    }

    @Override
    public List<ExtProcessDetail> getProcessDetail(String modelId) {
        return processDetailMapper.selectList(new QueryWrapper<ExtProcessDetail>().lambda().eq(ExtProcessDetail::getModelId, modelId));
    }
}
