package cn.e3mall.service.Impl;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemDescMapper itemDescMapper;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination topicDestination;
    @Autowired
    private JedisClient jedisClient;
    @Value("${REDIS_ITEM_PRE}")
    private String REDIS_ITEM_PRE;
    @Value("${ITEM_CACHE_EXPIRE}")
    private Integer ITEM_CACHE_EXPIRE;

    @Override
    public TbItem getItemById(long itemId) {
        try {
            String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":BASE");
            if (StringUtils.isNotBlank(json)) {
                TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
                return tbItem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //根据主键查询
        //TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //设置查询条件
        criteria.andIdEqualTo(itemId);
        //执行查询
        List<TbItem> list = itemMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            try {
                jedisClient.set(REDIS_ITEM_PRE+":" + itemId + ":BASE", JsonUtils.objectToJson(list.get(0)));
                jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":BASE", ITEM_CACHE_EXPIRE);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        final long itemId = IDUtils.genItemId();
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
        //发送商品添加消息
        jmsTemplate.send(topicDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(itemId + "");
                return textMessage;
            }
        });
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

    @Override
    public TbItemDesc getDesc(long id) {
        TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);

        return itemDesc;
    }

    @Override
    public E3Result updateItem(TbItem item, String desc) {

        item.setUpdated(new Date());
        itemMapper.updateByPrimaryKeySelective(item);

        TbItemDesc itemDesc = new TbItemDesc();
        itemDesc.setItemId(item.getId());
        itemDesc.setUpdated(new Date());
        itemDesc.setItemDesc(desc);
        itemDescMapper.updateByPrimaryKeySelective(itemDesc);
        return E3Result.ok();
    }

    @Override
    public TbItemDesc getItemDescById(long itemId) {
        try {
            String json = jedisClient.get(REDIS_ITEM_PRE + ":" + itemId + ":DESC");
            if (StringUtils.isNotBlank(json)) {
                TbItemDesc itemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
                return itemDesc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemId);
        try {
            jedisClient.set(REDIS_ITEM_PRE+":" + itemId + ":DESC", JsonUtils.objectToJson(itemDesc));
            jedisClient.expire(REDIS_ITEM_PRE + ":" + itemId + ":DESC", ITEM_CACHE_EXPIRE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDesc;
    }
}
