package cn.e3mall.service.Impl;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;

    @Override
    public TbItem getItemById(long itemId) {
        //根据主键查询
        //TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //设置查询条件
        criteria.andIdEqualTo(itemId);
        //执行查询
        List<TbItem> list = itemMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public EasyUIDataGridResult getItemList(int page, int rows) {
        PageHelper.startPage(page, rows);
        TbItemExample example = new TbItemExample();
        List<TbItem> list = itemMapper.selectByExample(example);
        EasyUIDataGridResult result = new EasyUIDataGridResult();
        result.setRows(list);
        PageInfo<TbItem> pageInfo = new PageInfo<>(list);
        long total = pageInfo.getTotal();
        result.setTotal(total);

        return result;
    }

    @Override
    public E3Result addItem(TbItem item, String desc) {
        long itemId = IDUtils.genItemId();
        item.setId(itemId);
        //1 正常   2 下架     3 删除
        item.setStatus((byte) 1);
        item.setCreated(new Date());
        item.setUpdated(new Date());
        itemMapper.insert(item);
        TbItemDesc itemDesc = new TbItemDesc();
        itemDesc.setItemId(itemId);
        itemDesc.setItemDesc(desc);
        itemDesc.setCreated(new Date());
        itemDesc.setUpdated(new Date());
        itemDescMapper.insert(itemDesc);
        return E3Result.ok();
    }

    @Override
    public E3Result deleteItem(String[] ids) {

        for (String id : ids) {
            long i = Long.parseLong(id);
            TbItem item=new TbItem();
            item.setId(i);
            item.setStatus((byte)3);
            itemMapper.updateByPrimaryKeySelective(item);
        }
        return E3Result.ok();
    }
    @Override
    public E3Result instockItem(String[] ids) {

        for (String id : ids) {
            long i = Long.parseLong(id);
            TbItem item=new TbItem();
            item.setId(i);
            item.setStatus((byte)2);
            itemMapper.updateByPrimaryKeySelective(item);
        }
        return E3Result.ok();
    }

    @Override
    public E3Result reshelfItem(String[] ids) {
        for (String id : ids) {
            long i = Long.parseLong(id);
            TbItem item=new TbItem();
            item.setId(i);
            item.setStatus((byte)1);
            itemMapper.updateByPrimaryKeySelective(item);
        }
        return E3Result.ok();
    }
}
