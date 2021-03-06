/******************************************************************************
 * 作者：kerwincui
 * 时间：2021-06-08
 * 邮箱：164770707@qq.com
 * 源码地址：https://gitee.com/kerwincui/wumei-smart
 * author: kerwincui
 * create: 2021-06-08
 * email：164770707@qq.com
 * source:https://github.com/kerwincui/wumei-smart
 ******************************************************************************/
package com.ruoyi.system.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.CustomException;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.domain.vo.DeviceControlCMD;
import com.ruoyi.system.domain.vo.IotDeviceListDto;
import com.ruoyi.system.service.IIotGroupService;
import com.ruoyi.system.service.TokenServiceCopy;
import com.ruoyi.system.service.impl.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.service.IIotDeviceService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import javax.validation.constraints.Size;

/**
 * 设备Controller
 *
 * @author kerwincui
 * @date 2021-05-06
 */
@Api(value = "设备", tags = "设备")
@RestController
@RequestMapping("/system/device")
public class IotDeviceController extends BaseController {
    @Autowired
    private IIotDeviceService iotDeviceService;

    @Autowired
    private IotDeviceDataServiceImpl iotDeviceDataService;

    @Autowired
    private IotThingsModelServiceImpl iotThingsModelService;

    @Autowired
    private IotDeviceModelServiceImpl  iotDeviceModelService;

    @Autowired
    private ThingsModelTemplateServiceImpl thingsModelTemplateService;

    @Autowired
    private IotDeviceGroupServiceImpl iotDeviceGroupService;

    @Autowired
    private TokenServiceCopy tokenServiceCopy;

    @Autowired
    private IIotGroupService iotGroupService;

    /**
     * 查询设备列表
     */
    @ApiOperation(value = "设备列表", notes = "设备列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(IotDevice iotDevice) {
        startPage();
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        iotDevice.setOwnerId(user.getUser().getUserId().toString());
        List<IotDeviceListDto> list = iotDeviceService.selectIotDeviceList(iotDevice);
        return getDataTable(list);
    }
    /**
     * 查询异常数据列表
     * 目前是可以看到所有的报警信息
     */
    @ApiOperation(value = "设备列表", notes = "设备列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/AlarmList")
    public TableDataInfo AlarmList(IotDevice iotDevice) {
        LoginUser loginUser = tokenServiceCopy.getLoginUser(ServletUtils.getRequest());
        IotGroup iotGroup = new IotGroup();
        IotDeviceData iotDeviceData = new IotDeviceData();
        /*if(!loginUser.getUser().isAdmin()) {
            Long userId = loginUser.getUser().getUserId();
            iotGroup.setUserId(userId);
        }
        startPage();
        List<IotGroup> list = iotGroupService.selectIotGroupList(iotGroup);
        for(int i = 0;i < list.size();i++){
        }*/
        iotDeviceData.setStatus("1");
        List<IotDeviceData> list = iotDeviceDataService.selectIotDeviceDataList(iotDeviceData);
        return getDataTable(list);
    }

    /**
     * 通过组id查询设备列表
     */
    @ApiOperation(value = "设备列表", notes = "设备列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/listDeviceByGroupId/{groupId}")
    public TableDataInfo listDeviceByGroupId(@PathVariable("groupId") long groupId) {
        IotDevice iotDevice = new IotDevice();
        iotDevice.setGroupId(groupId);
        startPage();
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        iotDevice.setOwnerId(user.getUser().getUserId().toString());
        List<IotDeviceListDto> list = iotDeviceService.selectIotDeviceList(iotDevice);
        return getDataTable(list);
    }
    @ApiOperation(value = "设备数据列表", notes = "设备数据列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/data/{deviceId}")
    public TableDataInfo listDeviceData(@PathVariable("deviceId") long deviceId) {
        List<List<IotDeviceData>> listAns = new ArrayList<>();
        IotDeviceModel t = new IotDeviceModel();
        t.setDeviceId(deviceId);
        startPage();
        List<IotDeviceModel> iotThingsModels = iotDeviceModelService.selectIotDeviceModelList(t);
        IotDeviceData iotDeviceData = new IotDeviceData();
        iotDeviceData.setDeviceId(deviceId);
        for(int i = 0;i < iotThingsModels.size(); i++){
            iotDeviceData.setModelId(iotThingsModels.get(i).getModelId());
            listAns.add(iotDeviceDataService.selectIotDeviceDataList(iotDeviceData));
        }
        return getDataTable(listAns);
    }

    @ApiOperation(value = "单一设备数据列表", notes = "设备数据列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/oneData/{deviceId}/{modelId}")
    public TableDataInfo listDeviceOneData(@PathVariable("deviceId") long deviceId,@PathVariable("modelId") long modelId) {
        startPage();
        IotDeviceData iotDeviceData = new IotDeviceData();
        iotDeviceData.setDeviceId(deviceId);
        iotDeviceData.setModelId(modelId);
        List<IotDeviceData> list = iotDeviceDataService.selectIotDeviceDataList(iotDeviceData);
        Collections.reverse(list);
        return getDataTable(list);
    }
    /**
     * 导出设备列表
     */
    @ApiOperation(value = "导出设备列表", notes = "导出设备列表")
    @PreAuthorize("@ss.hasPermi('system:device:export')")
    @Log(title = "设备", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(IotDevice iotDevice) {
        List<IotDeviceListDto> list = iotDeviceService.selectIotDeviceList(iotDevice);
        ExcelUtil<IotDeviceListDto> util = new ExcelUtil<IotDeviceListDto>(IotDeviceListDto.class);
        return util.exportExcel(list, "device");
    }

    /**
     * 获取设备详细信息
     */
    @ApiOperation(value = "获取设备详情", notes = "获取设备详情")
    @PreAuthorize("@ss.hasPermi('system:device:query')")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable("deviceId") Long deviceId) {
        return AjaxResult.success(iotDeviceService.selectIotDeviceById(deviceId));
    }

    /**
     * 根据设备编号获取设备详细信息
     */
    @ApiOperation(value = "根据设备编号获取设备详情", notes = "根据设备编号获取设备详情")
    @PreAuthorize("@ss.hasPermi('system:device:query')")
    @GetMapping(value = "/getByNum/{deviceNum}")
    public AjaxResult getInfoByNum(@PathVariable("deviceNum") String deviceNum) {
        return AjaxResult.success(iotDeviceService.selectIotDeviceByNum(deviceNum));
    }

    /**
     * 新增设备
     */
    @ApiOperation(value = "新增设备", notes = "新增设备")
    @PreAuthorize("@ss.hasPermi('system:device:add')")
    @Log(title = "设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody IotDevice iotDevice) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        iotDevice.setOwnerId(user.getUser().getUserId().toString());
        iotDevice.setStatus("未绑定");
        IotDevice device = iotDeviceService.selectIotDeviceByNum(iotDevice.getDeviceNum());
        if (device != null) {
            return AjaxResult.error("设备编号已存在，请重新填写");
        }
        iotDeviceService.insertIotDevice(iotDevice);
        /*插入模型ID*/
        if(iotDevice.getTemplateIds()!=null&&iotDevice.getTemplateIds().length>=1){
            Long[] templateIds = iotDevice.getTemplateIds();
            for (int i = 0; i < templateIds.length; i++) {
                Long templateId = templateIds[i];
                IotDeviceModel deviceModel = new IotDeviceModel();
                deviceModel.setDeviceId(iotDevice.getDeviceId());
                deviceModel.setModelId(templateId);
                iotDeviceModelService.insertIotDeviceModel(deviceModel);

            }
        }
        if(iotDevice.getGroupId()!=null){
            IotDeviceGroup iotDeviceGroup = new IotDeviceGroup();
            iotDeviceGroup.setDeviceId(iotDevice.getDeviceId());
            iotDeviceGroup.setGroupId(iotDevice.getGroupId());
            iotDeviceGroupService.insertIotDeviceGroup(iotDeviceGroup);

        }
        return AjaxResult.success();
    }
    /*获取设备对应的物模型*/
    @GetMapping("/deviceModel/{deviceId}")
    public AjaxResult getDeviceModel(@PathVariable("deviceId") long deviceId) {
        IotDeviceModel iotDeviceModel = new IotDeviceModel();
        iotDeviceModel.setDeviceId(deviceId);
        List<IotDeviceModel> iotDeviceModels = iotDeviceModelService.selectIotDeviceModelList(iotDeviceModel);
        if(iotDeviceModels==null) return AjaxResult.success();
        JSONArray ar = new JSONArray();
        for (int i = 0; i < iotDeviceModels.size(); i++) {
            JSONObject model = new JSONObject();
            model.put("templateId", iotDeviceModels.get(i).getModelId());
            ThingsModelTemplate thingsModelTemplate = thingsModelTemplateService.selectThingsModelTemplateByTemplateId(iotDeviceModels.get(i).getModelId());
            model.put("templateName", thingsModelTemplate.getTemplateName());
            ar.add(model);
        }
        return AjaxResult.success(ar);
    }

    /**
     * 修改设备
     */
    @ApiOperation(value = "修改设备", notes = "修改设备")
    @PreAuthorize("@ss.hasPermi('system:device:edit')")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody IotDevice iotDevice) {
        /*修改物模型  此段代码需要加在service层 不然不能一起回滚*/
        IotDeviceModel iotDeviceModel = new IotDeviceModel();
        iotDeviceModel.setDeviceId(iotDevice.getDeviceId());
        List<IotDeviceModel> iotDeviceModels = iotDeviceModelService.selectIotDeviceModelList(iotDeviceModel);
        if(iotDeviceModels!=null&&iotDeviceModels.size()>=1){
            for (int i = 0; i < iotDeviceModels.size(); i++) {
                iotDeviceModelService.deleteIotDeviceModelById(iotDeviceModels.get(i).getDeviceModelId());
            }
        }
        if(iotDevice.getTemplateIds()!=null&&iotDevice.getTemplateIds().length>=1){
            Long[] templateIds = iotDevice.getTemplateIds();
            for (int i = 0; i < templateIds.length; i++) {
                Long templateId = templateIds[i];
                IotDeviceModel deviceModel = new IotDeviceModel();
                deviceModel.setDeviceId(iotDevice.getDeviceId());
                deviceModel.setModelId(templateId);
                iotDeviceModelService.insertIotDeviceModel(deviceModel);

            }
        }
        return toAjax(iotDeviceService.updateIotDevice(iotDevice));
    }

    /**
     * 删除设备
     */
    @ApiOperation(value = "删除设备", notes = "删除设备")
    @PreAuthorize("@ss.hasPermi('system:device:remove')")
    @Log(title = "设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds) {
        return toAjax(iotDeviceService.deleteIotDeviceByIds(deviceIds));
    }


    @ApiOperation(value = "绑定设备", notes = "绑定设备")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PostMapping("/bindDevice")
    public AjaxResult bindDevice(@RequestBody IotDevice iotDevice) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
        String username = user.getUsername();
        String deviceNum = iotDevice.getDeviceNum();
        String deviceName = iotDevice.getDeviceName();
        Long categoryId = iotDevice.getCategoryId();
        String remark = iotDevice.getRemark();
        return AjaxResult.success(iotDeviceService.bindDevice(userId,username,deviceNum,deviceName,categoryId, remark));
    }

    @ApiOperation(value = "控制设备", notes = "控制设备")
    @PreAuthorize("@ss.hasPermi('system:device:control')")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PostMapping("/control")
    public AjaxResult control(@RequestBody DeviceControlCMD deviceControlCMD) {
        String deviceNum = deviceControlCMD.getDeviceNum();
        String cmd = deviceControlCMD.getCmd();
        if(StringUtils.isBlank(deviceNum)) {
            throw new CustomException("设备编号不能为空");
        }
        if(StringUtils.isBlank(cmd)) {
            throw new CustomException("控制指令不能为空");
        }
        return toAjax(iotDeviceService.controlDeviceByNum(deviceNum,cmd));
    }



    @ApiOperation(value = "小程序设备列表", notes = "小程序设备列表")
    @PreAuthorize("@ss.hasPermi('system:device:list')")
    @GetMapping("/listDevice")
    public TableDataInfo listDevice(IotDevice iotDevice) {
        startPage();
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        iotDevice.setOwnerId(user.getUser().getUserId().toString());
        List<IotDeviceListDto> list = iotDeviceService.selectMpIotDeviceList(iotDevice);
        return getDataTable(list);
    }

    @ApiOperation(value = "修改设备名称和备注", notes = "修改设备名称和备注")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PostMapping("/updateDeviceInfo")
    public AjaxResult updateDeviceInfo(@RequestBody IotDevice iotDevice) {
        Long deviceId = iotDevice.getDeviceId();
        String deviceName = iotDevice.getDeviceName();
        String remark = iotDevice.getRemark();
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
         String nickName = user.getUser().getNickName();
        return AjaxResult.success(iotDeviceService.updateDeviceInfo( userId,  nickName,  deviceId,  deviceName,  remark));
    }

    @ApiOperation(value = "根据用户设备编号获取设备详情", notes = "根据设备编号获取设备详情")
    @GetMapping(value = "/getByUserAndNum/{deviceNum}")
    public AjaxResult getInfoByUserAndNum(@PathVariable("deviceNum") String deviceNum) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
        return AjaxResult.success(iotDeviceService.selectIotDeviceByUserAndNum(userId,deviceNum));
    }

    @ApiOperation(value = "解除绑定设备", notes = "解除绑定设备")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @PostMapping("/unBindDevice")
    public AjaxResult unBindDevice(@RequestBody IotDevice iotDevice) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
        String username = user.getUsername();
        Long deviceId = iotDevice.getDeviceId();
        return AjaxResult.success(iotDeviceService.unBindDevice(userId,username,deviceId));
    }

    @ApiOperation(value = "根据设备编号查询设备信息", notes = "根据设备编号查询设备信息")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @GetMapping("/getDeviceInfoByDeviceNum")
    public AjaxResult getDeviceInfoByDeviceNum(@RequestParam String deviceNum ) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
        String username = user.getUsername();
        return AjaxResult.success(iotDeviceService.getDeviceInfoByDeviceNum(userId,username,deviceNum));
    }

    @ApiOperation(value = "根据设备编号查询设备信息", notes = "根据设备编号查询设备信息")
    @Log(title = "设备", businessType = BusinessType.UPDATE)
    @GetMapping("/getDeviceInfoByDeviceId")
    public AjaxResult getDeviceInfoByDeviceId(@RequestParam Long deviceId ) {
        LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getUser().getUserId();
        String username = user.getUsername();
        return AjaxResult.success(iotDeviceService.getDeviceInfoByDeviceId(userId,username,deviceId));
    }

}
