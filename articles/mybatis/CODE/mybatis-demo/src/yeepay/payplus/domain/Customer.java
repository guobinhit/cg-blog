package yeepay.payplus.domain;

import java.util.List;

/**
 * Created by 维C果糖 on 2017/4/6.
 */
public class Customer {
    private List<Orders> orders;    // 关联多个订单
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Orders> getOrders() {
        return orders;
    }

    public void setOrders(List<Orders> orders) {
        this.orders = orders;
    }
}
