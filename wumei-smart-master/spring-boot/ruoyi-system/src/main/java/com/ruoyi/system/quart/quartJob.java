package com.ruoyi.system.quart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.IotDeviceData;
import com.ruoyi.system.domain.IotDeviceModel;
import com.ruoyi.system.domain.ThingsModelTemplate;
import com.ruoyi.system.service.IIotDeviceService;
import com.ruoyi.system.service.impl.IotDeviceModelServiceImpl;
import com.ruoyi.system.service.impl.ThingsModelTemplateServiceImpl;
import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ruoyi.common.utils.Threads.sleep;

@Component("quartJob")
public class quartJob {

    @Autowired
    private ThingsModelTemplateServiceImpl thingsModelTemplateService;

    @Autowired
    private IotDeviceModelServiceImpl iotDeviceModelService;

    @Autowired
    private IIotDeviceService iotDeviceService;

    @Autowired
    RedisCache redisCache;

    public void ryDevice(Long deviceId){
        IotDeviceModel t = new IotDeviceModel();
        t.setDeviceId(deviceId);
        List<IotDeviceModel> iotThingsModels = iotDeviceModelService.selectIotDeviceModelList(t);
        Long group_Id = iotDeviceService.selectIotDeviceById(deviceId).getGroupId();
        for(int i = 0;i < iotThingsModels.size(); i++){
            ThingsModelTemplate model = thingsModelTemplateService.selectThingsModelTemplateByTemplateId(iotThingsModels.get(i).getModelId());
            JSONObject specsJson = JSON.parseObject(model.getSpecs());
            try {
                String atcmd = specsJson.get("atcmd").toString();
                String index = specsJson.get("index").toString();
                /*
                * ??????????????????
                * ????????????????????????????????????????????????redis????????????????????????????????????
                */
                Map<String,Object> map = new HashedMap<String,Object>();
                map.put("specs",model.getSpecs());
                map.put("modelId",model.getTemplateId());
                map.put("identifier",model.getIdentifier());
                //????????????
                redisCache.setCacheMap(group_Id + "_" + deviceId, map);
                SendMessage(atcmd);
                /*redisCache.expire(group_Id + "_" + deviceId, 4);*/
                Thread.sleep(4000);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("????????????");
            }
        }
    }
    public byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            //??????
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            //??????
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
    public  byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
    public void SendMessage(String content){
        String topic = "pull_group_15";
        String broker = "tcp://1.116.1.99:61613";
        int qos = 1;
        String userName = "admin";
        String password = "password";
        String clientId = "pubClient2";
        // ????????????
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            // ???????????????
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            // ??????????????????
            MqttConnectOptions connOpts = new MqttConnectOptions();
            // ?????????????????????????????????????????????
            connOpts.setCleanSession(false);
            // ????????????????????????
            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());
            // ????????????
            System.out.println("????????? broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("????????????");
            // ????????????
            MqttMessage message = new MqttMessage(hexToByteArray(content));
            // ???????????????????????????
            message.setQos(qos);
            // ????????????
            System.out.println("???" + topic + "????????????:" + message);
            sampleClient.publish(topic, message);
            // ????????????
            sampleClient.disconnect();
            // ???????????????
            sampleClient.close();
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}
