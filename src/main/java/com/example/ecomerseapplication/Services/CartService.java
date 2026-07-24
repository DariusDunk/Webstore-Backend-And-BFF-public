package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.Cart;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getOrCreateByCustomer(Customer customer) {
        return cartRepository.getCartOwnerByCustomer(customer)
                .orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    public Cart save(Cart cart) {
       return cartRepository.save(cart);
    }

    public Optional<Cart> getBySession(Session session) {
        return cartRepository.getBySession(session);
    }

    public void deleteCartsBySessions(List<Session> sessions) {

        List<String> sessionIds = sessions.stream().map(Session::getSessionId).toList();

        cartRepository.deleteBySessions(sessionIds);
    }

    public void deleteCartBySession(String sessionId) {
        cartRepository.deleteBySession(sessionId);
    }

    public void deleteCartByCustomer(Customer customer) {
        cartRepository.deleteByCustomer(customer);
    }

    public boolean existsBySession(Session session) {
        return cartRepository.existsBySession(session);
    }

    public Cart getBySessionOptional(Session session) {
        return cartRepository.getBySession(session).orElse(null);
    }
}
