package cn.e3mall.controller;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping("/item/{itemId}")
    @ResponseBody
    public TbItem getItemById(@PathVariable Long itemId) {
        TbItem tbItem = itemService.getItemById(itemId);
        return tbItem;
    }

    @RequestMapping("/item/list")
    @ResponseBody
    public EasyUIDataGridResult getItemList(Integer page ,Integer rows){
        EasyUIDataGridResult result = itemService.getItemList(page, rows);
        return result;
    }

    @RequestMapping(value = "/item/save", method = RequestMethod.POST)
    @ResponseBody
    public E3Result addItem(TbItem item, String desc) {
        E3Result result = itemService.addItem(item, desc);
        return result;
    }

    @RequestMapping("/rest/item/delete")
    @ResponseBody
    public E3Result deleteItem(String[] ids){
        E3Result e3Result = itemService.deleteItem(ids);
        return e3Result;
    }


    @RequestMapping("/rest/item/instock")
    @ResponseBody
    public E3Result instockItem(String[] ids){
        E3Result e3Result = itemService.instockItem(ids);
        return e3Result;
    }

    @RequestMapping("/rest/item/reshelf")
    @ResponseBody
    public E3Result reshelfItem(String[] ids){
        E3Result e3Result = itemService.reshelfItem(ids);
        return e3Result;
    }
}
