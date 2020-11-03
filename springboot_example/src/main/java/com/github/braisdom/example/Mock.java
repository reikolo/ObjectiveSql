package com.github.braisdom.example;

import com.github.braisdom.example.model.Member;
import com.github.braisdom.example.model.Order;
import com.github.braisdom.example.model.OrderLine;
import com.github.braisdom.example.model.Product;
import com.github.braisdom.objsql.ConnectionFactory;
import com.github.braisdom.objsql.Databases;
import com.sun.tools.corba.se.idl.constExpr.Or;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mock {

    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    private static final String[] MEMBER_NAMES = {"Joe", "Juan", "Jack", "Albert", "Jonathan", "Justin", "Terry", "Gerald", "Keith", "Samuel",
            "Willie", "Ralph", "Lawrence", "Nicholas", "Roy", "Benjamin", "Bruce", "Brandon", "Adam", "Harry", "Fred", "Wayne", "Billy", "Steve",
            "Louis", "Jeremy", "Aaron", "Randy", "Howard", "Eugene", "Carlos", "Russell", "Bobby", "Victor", "Martin", "Ernest", "Phillip", "Todd",
            "Jesse", "Craig", "Alan", "Shawn", "Clarence", "Sean", "Philip", "Chris", "Johnny", "Earl", "Jimmy", "Antonio", "James", "John", "Robert",
            "Michael", "William", "David", "Richard", "Charles", "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George",
            "Kenneth", "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy", "Jose", "Larry", "Jeffrey",
            "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory", "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold",
            "Douglas", "Henry", "Carl", "Arthur", "Ryan", "Roger"};

    private static final String[] PRODUCT_NAMES = {
            "face wash", "toner", "firming lotion", "smoothing toner", "moisturizers and creams", "moisturizer",
            "sun screen", "eye gel", "facial mask", "Lip care", "Lip coat", "facial scrub", "bodylotion", "Shower Gel",
            "eye shadow", "mascara", "lip liner", "makeup remover ", "makeup removing lotion", "baby diapers", "milk powder",
            "toothbrush", "toothpaste", "wine", "beer", "Refrigerator", "television", "Microwave Oven", "rice cooker",
            "coffee", "tea", "milk", "drink", "whisky", "tequila", "Liquid soap"
    };

    private static final String[] SALES_TIMES = {
            "2020-09-01 13:41:01", "2020-09-01 09:23:34", "2020-09-02 10:15:59", "2020-09-02 15:54:12",
            "2020-09-03 08:41:03", "2020-09-03 16:33:09", "2020-09-04 09:13:41", "2020-09-04 12:01:23",
            "2019-09-01 13:41:01", "2019-09-01 09:23:34", "2019-09-02 10:15:59", "2019-09-02 15:54:12",
            "2019-09-03 08:41:03", "2019-09-03 16:33:09", "2019-09-04 09:13:41", "2019-09-04 12:01:23",
    };

    public void generateData() throws SQLException {
        List<Member> members = generateMembers();
        List<Product> products = generateProducts();

        generateOrdersAndOrderLines(members, products);
    }

    private List<Member> generateMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i < MEMBER_NAMES.length; i++) {
            Member member = new Member();
            members.add(member.setId(Long.valueOf(i))
                    .setNo(String.format("M20200000%s", (i + 1)))
                    .setName(MEMBER_NAMES[i])
                    .setGender(RandomUtils.nextInt(1, 3))
                    .setMobile(getMobile()));
        }
        Member.create(members.toArray(new Member[]{}), false, true);

        return members;
    }

    private List<Product> generateProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i < PRODUCT_NAMES.length; i++) {
            Product product = new Product();
            products.add(product.setId(Long.valueOf(i))
                    .setName(PRODUCT_NAMES[i])
                    .setBarcode(String.format("6901234567%s", (i + 10)))
                    .setCategoryId(RandomUtils.nextInt(1, 10))
                    .setCost(RandomUtils.nextFloat(5.0f, 40.0f))
                    .setSalesPrice(RandomUtils.nextFloat(10.0f, 50.0f)));
        }
        Product.create(products.toArray(new Product[]{}), false, true);
        return products;
    }

    private void generateOrdersAndOrderLines(List<Member> members, List<Product> products) throws SQLException {
        List<Order> orders = new ArrayList<>();
        List<OrderLine> orderLines = new ArrayList<>();

        for (int i = 1; i < 500; i++) {
            Order order = new Order();
            Member member = members.get(RandomUtils.nextInt(1, members.size()));

            int orderLineCount = RandomUtils.nextInt(2, 5);
            float totalAmount = 0;
            float totalQuantity = 0;

            order.setId(Long.valueOf(i))
                    .setNo("O0000000" + i);

            for (int t = 1; t < orderLineCount; t++) {
                Product product = products.get(RandomUtils.nextInt(0, products.size()));
                int productQuantity = RandomUtils.nextInt(1, 5);
                float productAmount = productQuantity * product.getSalesPrice();

                OrderLine orderLine = new OrderLine();
                orderLine.setId(Long.valueOf(t))
                        .setAmount(productAmount)
                        .setQuantity(Float.valueOf(productQuantity))
                        .setProductId(product.getId())
                        .setBarcode(product.getBarcode())
                        .setSalesPrice(product.getSalesPrice())
                        .setMemberId(member.getId())
                        .setOrderId(order.getId())
                        .setOrderNo(order.getNo());

                totalAmount += productAmount;
                totalQuantity += productQuantity;

                orderLines.add(orderLine);
            }

            order.setAmount(totalAmount)
                    .setQuantity(totalQuantity)
                    .setMemberId(member.getId())
                    .setSalesAt(Timestamp.valueOf(SALES_TIMES[RandomUtils.nextInt(0, SALES_TIMES.length)]));

            orders.add(order);
        }

        Order.create(orders.toArray(new Order[]{}), true, true);
        OrderLine.create(orderLines.toArray(new OrderLine[]{}), true, true);
    }

    private static String getMobile() {
        int index = getRandomNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getRandomNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getRandomNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }

    public static int getRandomNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    public static void main(String[] args) throws SQLException {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://localhost:4406/objective_sql");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("123456");
        DataSource dataSource = dataSourceBuilder.build();

        Databases.installConnectionFactory(new ConnectionFactory() {
            @Override
            public Connection getConnection(String dataSourceName) throws SQLException {
                return dataSource.getConnection();
            }
        });

        Databases.execute("TRUNCATE TABLE members");
        Databases.execute("TRUNCATE TABLE products");
        Databases.execute("TRUNCATE TABLE orders");
        Databases.execute("TRUNCATE TABLE order_lines");

        new Mock().generateData();
    }
}
