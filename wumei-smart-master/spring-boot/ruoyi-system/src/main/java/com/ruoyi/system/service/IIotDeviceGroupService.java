package com.ruoyi.system.service;

import java.util.List;

import com.ruoyi.system.domain.IotDeviceGroup;

/**
 * 【请填写功能名称】Service接口
 *
 * @author wxy
 * @date 2022-03-24
 */
public interface IIotDeviceGroupService {
    /**
     * 查询【请填写功能名称】
     *
     * @param deviceGroupId 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    public IotDeviceGroup selectIotDeviceGroupById(Long deviceGroupId);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param iotDeviceGroup 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<IotDeviceGroup> selectIotDeviceGroupList(IotDeviceGroup iotDeviceGroup);

    /**
     * 新增【请填写功能名称】
     *
     * @param iotDeviceGroup 【请填写功能名称】
     * @return 结果
     */
    public int insertIotDeviceGroup(IotDeviceGroup iotDeviceGroup);

    /**
     * 修改【请填写功能名称】
     *
     * @param iotDeviceGroup 【请填写功能名称】
     * @return 结果
     */
    public int updateIotDeviceGroup(IotDeviceGroup iotDeviceGroup);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param deviceGroupIds 需要删除的【请填写功能名称】ID
     * @return 结果
     */
    public int deleteIotDeviceGroupByIds(Long[] deviceGroupIds);

    /**
     * 删除【请填写功能名称】信息
     *
     * @param deviceGroupId 【请填写功能名称】ID
     * @return 结果
     */
    public int deleteIotDeviceGroupById(Long deviceGroupId);
}
