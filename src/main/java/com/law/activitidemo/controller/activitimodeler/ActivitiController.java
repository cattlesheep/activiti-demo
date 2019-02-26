package com.law.activitidemo.controller.activitimodeler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/activiti")
public class ActivitiController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    ProcessEngine processEngine;
    @Autowired
    ObjectMapper objectMapper;
    /**
     * 创建模型
     */
    @RequestMapping("/create")
    public void create(HttpServletRequest request, HttpServletResponse response) {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

            RepositoryService repositoryService = processEngine.getRepositoryService();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "hello1111");
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            String description = "hello1111";
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName("hello1111");
            modelData.setKey("12313123");

            //保存模型
            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            String modelerStr = request.getContextPath() + "/modeler.html?modelId=" + modelData.getId();
            response.sendRedirect(modelerStr);
        } catch (Exception e) {
            System.out.println("创建模型失败：");
        }
    }

    /**
     * 获取所有模型
     * @return
     */
    @GetMapping
    public Object modelList(){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<Model> models = repositoryService.createModelQuery().list();
        return models;
    }

    /**
     * 删除模型
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public Object deleteModel(@PathVariable("id")String id){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.deleteModel(id);
        return "删除成功！";
    }

    /**
     * 发布模型为流程定义
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("{id}/deployment")
    public Object deploy(@PathVariable("id")String id) throws Exception {

        //获取模型
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return "SUCCESS";
    }
    /**
     *  启动流程
     */
    @RequestMapping("/start")
    @ResponseBody
    public Object startProcess(String keyName) {
        ProcessInstance process = processEngine.getRuntimeService().startProcessInstanceByKey(keyName);
        return process.getId() + " : " + process.getProcessDefinitionId();
    }

    /**
     *  提交任务
     */
    @RequestMapping("/run")
    @ResponseBody
    public Object run(String processInstanceId) {
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).singleResult();
        processEngine.getTaskService().complete(task.getId());
        return "SUCCESS";
    }
    @RequestMapping(value = "editor/{modelId}")
    public String editor(@PathVariable("modelId") String modelId) {
        ModelAndView mav = new ModelAndView();
        String XML = "";
        try {
            Model modelData = repositoryService.getModel(modelId);
            String  description = modelData.getMetaInfo().substring(modelData.getMetaInfo().lastIndexOf(":")+2,modelData.getMetaInfo().lastIndexOf("}")-1);
            modelData.setMetaInfo(description);
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree((new String(modelEditorSource,"UTF-8")).getBytes("UTF-8"));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] exportBytes = xmlConverter.convertToXML(bpmnModel,"UTF-8");

            XML =  new String(exportBytes,"UTF-8");
            mav.setViewName("bpm/modelEditor");
            mav.addObject("data",XML);
            mav.addObject("model",modelData);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return XML;
    }
}