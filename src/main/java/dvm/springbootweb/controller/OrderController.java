package dvm.springbootweb.controller;

import dvm.springbootweb.entity.OrderDetail;
import dvm.springbootweb.dto.OrderDto;
import dvm.springbootweb.entity.Cart;
import dvm.springbootweb.entity.Order;
import dvm.springbootweb.entity.User;
import dvm.springbootweb.jwt.JwtTokenProvider;
import dvm.springbootweb.payload.response.MessageResponse;
import dvm.springbootweb.repository.OrderDetailRepository;
import dvm.springbootweb.service.CartService;
import dvm.springbootweb.service.OrderService;
import dvm.springbootweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailRepository detailRepository;

    @Autowired
    private CartService cartService;

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> viewOrder(@RequestHeader("Authorization") String token) {
        String userName = jwtTokenProvider.getUserNameFromJwt(token.substring(7));
        User user = userService.findByUserName(userName);
        List<Order> orderList = orderService.getAllOrderByUser(user);
        return ResponseEntity.ok(orderList);
    }
    @GetMapping("/page-order")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String,Object>> viewPaggingOrder(@RequestParam(required = true) int page){
        int size = 3;
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> pageCurrent = orderService.getPagging(pageable);
        Map<String, Object> data = new HashMap<>();
        data.put("orders", pageCurrent.getContent());
        data.put("totalPages", pageCurrent.getTotalPages());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/adminOrder")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> viewAdminOrder(@RequestHeader("Authorization") String token) {
        boolean isValid = jwtTokenProvider.validateJwtToken(token.substring(7));
        if(isValid) {
            List<Order> orderList = orderService.findAll();
            return ResponseEntity.ok(orderList);
        }
        return ResponseEntity.badRequest().body(new MessageResponse("token is not valid"));
    }

    @GetMapping("order/{orderId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> viewOrderDetail(@PathVariable int orderId, @RequestHeader("Authorization")String token){
        List<OrderDetail> orDetailList = null;
        orDetailList = detailRepository.findAllByOrderId(orderId);
        return ResponseEntity.ok(orDetailList);
    }
    @PostMapping("/addToOrder")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addToOrder(@RequestBody OrderDto orderDto, @RequestHeader("Authorization") String token) {
        String token1 = token.substring(7);
        String userName = jwtTokenProvider.getUserNameFromJwt(token1);
        User user = userService.findByUserName(userName);
//        save order and update sold
        orderService.save(orderDto, user);
//        delete cart
        cartService.deleteAllCart(user.getUserId());
        return ResponseEntity.ok(new MessageResponse("order added"));
    }
    @PutMapping("updateOrder/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateStatusOrder(@PathVariable int id, @RequestHeader("Authorization") String token, @RequestParam String status){
        boolean isValid = jwtTokenProvider.validateJwtToken(token.substring(7));
        if(isValid) {
            Order order = orderService.findById(id);
            order.setStatus(status);
            orderService.update(order);
            return ResponseEntity.ok(new MessageResponse("update order successfully"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("update not successfully"));
    }
    @DeleteMapping("deleteOrder/{id}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable int id){
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new MessageResponse("delete order successfully"));
    }
}
