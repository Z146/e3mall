package cn.e3mall.cart.controller;

import cn.e3mall.cart.service.CartService;
import cn.e3mall.common.utils.CookieUtils;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbUser;
import cn.e3mall.service.ItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private ItemService itemService;
    @Value("${COOKIE_CART_EXPIRE}")
    private Integer COOKIE_CART_EXPIRE;
    @Autowired
    private CartService cartService;

    @RequestMapping("/cart/add/{itemId}")
    public String addCart(@PathVariable Long itemId, @RequestParam(defaultValue = "1") Integer num,
                          HttpServletRequest request, HttpServletResponse response) {

        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            cartService.addCart(user.getId(), itemId, num);
            return "cartSuccess";
        }

        List<TbItem> itemList = getCartListFromCookie(request);
        boolean flag=false;
        for (TbItem item : itemList) {
            if (item.getId() == itemId.longValue()) {
                flag = true;
                item.setNum(item.getNum() + num);
                break;
            }
        }
        if (!flag) {
            TbItem tbItem = itemService.getItemById(itemId);
            tbItem.setNum(num);
            String image = tbItem.getImage();
            if (StringUtils.isNotBlank(image)) {
                tbItem.setImage(image.split(",")[0]);
            }
            itemList.add(tbItem);
        }
        CookieUtils.setCookie(request, response, "cart",
                JsonUtils.objectToJson(itemList), COOKIE_CART_EXPIRE,true);
        return "cartSuccess";
    }

    private List<TbItem> getCartListFromCookie(HttpServletRequest request) {
        String json = CookieUtils.getCookieValue(request, "cart", true);
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        List<TbItem> itemList = JsonUtils.jsonToList(json, TbItem.class);
        return itemList;
    }


    @RequestMapping("/cart/cart")
    public String showCartList(HttpServletRequest request,HttpServletResponse response){
        List<TbItem> cartList = getCartListFromCookie(request);
        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            cartService.mergeCart(user.getId(), cartList);
            CookieUtils.deleteCookie(request, response, "cart");
            cartList= cartService.getCartList(user.getId());
        }
        request.setAttribute("cartList", cartList);
        return "cart";
    }

    @RequestMapping("/cart/update/num/{itemId}/{num}")
    @ResponseBody
    public E3Result updateCartNum(@PathVariable Long itemId, @PathVariable Integer num,
                                  HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否为登录状态
        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            cartService.updateCartNum(user.getId(), itemId, num);
            return E3Result.ok();
        }
        List<TbItem> itemList = getCartListFromCookie(request);
        for (TbItem item : itemList) {
            if (item.getId() == itemId.longValue()) {
                item.setNum(num);
                break;
            }
        }
        CookieUtils.setCookie(request, response, "cart", JsonUtils.objectToJson(itemList), COOKIE_CART_EXPIRE,true);
        return E3Result.ok();
    }

    @RequestMapping("/cart/delete/${cartId}")
    public String deleteCartItem(@PathVariable Long itemId, HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否为登录状态
        TbUser user = (TbUser) request.getAttribute("user");
        if (user != null) {
            cartService.deleteCartItem(user.getId(), itemId);
            return "redirect:/cart/cart.html";
        }
        List<TbItem> itemList = getCartListFromCookie(request);
        for (TbItem item : itemList) {
            if (item.getId() == itemId.longValue()) {
                itemList.remove(item);
                break;
            }
        }
        CookieUtils.setCookie(request, response, "cart", JsonUtils.objectToJson(itemList), COOKIE_CART_EXPIRE,true);
        return "redirect:/cart/cart.html";
    }

}
