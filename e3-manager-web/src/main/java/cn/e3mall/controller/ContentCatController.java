package cn.e3mall.controller;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ContentCatController {

    @Autowired
    private ContentCategoryService categoryService;

    @RequestMapping("/content/category/list")
    @ResponseBody
    public List<EasyUITreeNode> getContentCatList(@RequestParam(name = "id" ,defaultValue = "0")Long parentId) {
        List<EasyUITreeNode> list = categoryService.getContentCatList(parentId);
        return list;
    }

    @RequestMapping(value = "/content/category/create", method = RequestMethod.POST)
    @ResponseBody
    public E3Result createContentCategory(Long parentId, String name) {
        E3Result e3Result = categoryService.addContentCategory(parentId, name);
        return e3Result;

    }
    //content/category/update
    @RequestMapping(value = "/content/category/update", method = RequestMethod.POST)
    public void updateContentCategory(Long id, String name) {
        categoryService.updateContentCategory(id, name);
    }

    @RequestMapping("/content/category/delete")
    @ResponseBody
    public E3Result deleteContentCategory(Long id){
        E3Result e3Result = categoryService.deleteContentCategory(id);
        return e3Result;
    }
}
