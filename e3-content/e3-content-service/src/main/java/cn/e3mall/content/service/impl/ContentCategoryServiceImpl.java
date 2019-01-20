package cn.e3mall.content.service.impl;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentCategoryService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentCategoryExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper contentCategoryMapper;
    @SuppressWarnings("Duplicates")
    @Override
    public List<EasyUITreeNode> getContentCatList(long parentId) {
        TbContentCategoryExample example = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
        List<EasyUITreeNode> nodes = new ArrayList<>();
        for (TbContentCategory category : list) {
            if (category.getStatus()==2){
                continue;
            }
            EasyUITreeNode node = new EasyUITreeNode();
            node.setId(category.getId());
            node.setText(category.getName());
            node.setState(category.getIsParent() ? "closed" : "open");
            nodes.add(node);
        }
        return nodes;

    }

    @Override
    public E3Result addContentCategory(long parentId, String name) {
        TbContentCategory category = new TbContentCategory();
        category.setParentId(parentId);
        category.setIsParent(false);
        category.setName(name);
        //1 正常     2  删除
        category.setStatus(1);
        category.setSortOrder(1);
        category.setCreated(new Date());
        category.setUpdated(new Date());

        contentCategoryMapper.insert(category);
        TbContentCategory parent = contentCategoryMapper.selectByPrimaryKey(parentId);
        if (!parent.getIsParent()) {
            parent.setIsParent(true);
            //contentCategoryMapper.updateByPrimaryKey(parent);
            contentCategoryMapper.updateByPrimaryKeySelective(parent);
        }

        return E3Result.ok(category);

    }

    @Override
    public void updateContentCategory(Long id, String name) {
        TbContentCategory category = contentCategoryMapper.selectByPrimaryKey(id);
        category.setName(name);
        contentCategoryMapper.updateByPrimaryKeySelective(category);

    }

    @Override
    public E3Result deleteContentCategory(Long id) {
        TbContentCategory category = contentCategoryMapper.selectByPrimaryKey(id);
        if (!category.getIsParent()){
            category.setStatus(2);
            contentCategoryMapper.updateByPrimaryKeySelective(category);
            return E3Result.ok();
        }else{
            return E3Result.build(300, "你选择的是父节点，请先删除子节点！");
        }
    }
}
