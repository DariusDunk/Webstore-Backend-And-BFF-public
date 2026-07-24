package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CompositeIdClasses.CartProductId;
import com.example.ecomerseapplication.DTOs.responses.CartItemResponse;
import com.example.ecomerseapplication.DTOs.responses.CartSummaryResponse;
import com.example.ecomerseapplication.DTOs.responses.MessageResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.CartItemDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CartSummaryItem;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.CartLimitReachedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.NoStockForCartException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.StockExceededException;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Repositories.CartProductRepository;
import com.example.ecomerseapplication.enums.ResultTypes;
import jakarta.persistence.EntityManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartProductService {


    private final CartProductRepository cartProductRepository;
    private final CartService cartService;
    private final EntityManager entityManager;
    private final SessionCartService sessionCartService;

    @Autowired
    public CartProductService(CartProductRepository cartProductRepository, CartService cartService, EntityManager entityManager, SessionCartService sessionCartService) {
        this.cartProductRepository = cartProductRepository;
        this.cartService = cartService;
        this.entityManager = entityManager;
        this.sessionCartService = sessionCartService;
    }

    @Transactional
    public String  addToOrRemoveFromCart(Customer customer, Product product, Boolean doIncrement) {

        Cart cart = cartService.getOrCreateByCustomer(customer);

        CartProductId cartId = new CartProductId(product, cart);

        CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);

        try
        {
            if (cartProduct == null) {

                if (cartsByCustomer(customer).size() >= GlobalConstants.CART_SIZE_LIMIT)
                    throw new CartLimitReachedException("Cart limit reached!");

                cartProduct = new CartProduct(cartId, (short) 1);
                cartProductRepository.save(cartProduct);
                return "Успешно добавен в количката!";
            }

            return doIncrementMainLogic(product, doIncrement, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof CartLimitReachedException
                    || e instanceof StockExceededException) {
                throw e;
            }

            throw new RuntimeException(e);
        }
    }

    @NonNull
    private String doIncrementMainLogic(Product product, Boolean doIncrement, CartProductId cartId, CartProduct cartProduct) {
        if (doIncrement) {
            short quantity = cartProduct.getQuantity();
            if (product.getQuantityInStock() < quantity + 1)
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");

            cartProduct.setQuantity(++quantity);
            cartProductRepository.save(cartProduct);
            return "Успешно увеличeно количество в количката!";
        }

        if (cartProduct.getQuantity() == 1) {
            cartProductRepository.deleteById(cartId);
            return "Успешно премахнат от количката!";
        } else {
            short quantity = cartProduct.getQuantity();
            cartProduct.setQuantity(--quantity);

            return "Успешно намалено количество в коликчата!";
        }
    }

    @Transactional
    public String  addToOrRemoveFromCart(Session session, Product product, Boolean doIncrement) {

        Cart cart = sessionCartService.getOrCreateSessionCart(session);
        CartProductId cartId = new CartProductId(product, cart);
        CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);

        try
        {
            if (cartProduct == null) {

                if (doIncrement)
                {
                    if (cartsBySession(session).size() >= GlobalConstants.CART_SIZE_LIMIT)
                        throw new CartLimitReachedException("Cart limit reached!");

                    cartProduct = new CartProduct(cartId, (short) 1);
                    cartProductRepository.save(cartProduct);
                    return "Успешно добавен в количката!";
                }
                else
                {
                    return "Успешно премахнат от количката! ";
                }
            }

            return doIncrementMainLogic(product, doIncrement, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof CartLimitReachedException
                    || e instanceof StockExceededException) {
                throw e;
            }

            throw new RuntimeException(e);
        }
    }

    public boolean cartItemExistsByCustomer(Customer customer, Product product) {
        Cart cart = cartService.getOrCreateByCustomer(customer);
        return cartProductRepository.existsByCartProductId(new CartProductId(product, cart));
    }

    public List<CartProduct> cartsByCustomer(Customer customer) {
        return cartProductRepository.findByCustomer(customer.getKeycloakId());
    }

    public List<CartProduct> cartsBySession(Session session) {
        return cartProductRepository.getBySession(session);
    }

    public List<CartItemResponse> getCartDtoByCustomer(Customer customer) {
        List<CartItemDTO> cartItemDTOList = cartProductRepository.findDtoByCustomer(customer.getKeycloakId());
        return ProductDTOMapper.cartItemtListToCartItemResponseList(cartItemDTOList);
    }

    public List<CartItemResponse> getCartDtoBySession(Session session) {
        List<CartItemDTO> cartItemDTOList = cartProductRepository.findDtoBySession(session);

       return ProductDTOMapper.cartItemtListToCartItemResponseList(cartItemDTOList);
    }

    @Transactional
    public List<CartItemResponse> removeFromCartWFetch(Customer customer, String productCode) {
        Cart cart = cartService.getOrCreateByCustomer(customer);
        cartProductRepository.deleteByCartAndProductCode(cart, productCode);

        return getCartDtoByCustomer(customer);
    }

    @Transactional
    public List<CartItemResponse> removeFromCartWFetch(Session session, String productCode) {
        Cart cart = sessionCartService.getOrCreateSessionCart(session);
        cartProductRepository.deleteByCartAndProductCode(cart, productCode);

        return getCartDtoBySession(session);
    }

    @Transactional
    public MessageResponse addBatchToCart(Customer customer, List<Product> products) {

        boolean stockExceeded = false;
        Cart cart = cartService.getOrCreateByCustomer(customer);
        List<CartProduct> cartProducts = cartsByCustomer(customer);
        return cartBatchAddLogic(products, cartProducts, cart, stockExceeded);

    }

    @NonNull
    private MessageResponse cartBatchAddLogic(List<Product> products, List<CartProduct> cartProducts, Cart cart, boolean stockExceeded) {
        int originalSize = cartProducts.size();
        Map<CartProductId, CartProduct> cartMap = Map.copyOf(cartProducts.stream().collect(HashMap::new,
                (m, v) -> m.put(v.getCartProductId(), v), HashMap::putAll));

        cartProducts = new ArrayList<>();

//        System.out.println("CART MAP size: " + cartMap.size());

        StringBuilder sb = new StringBuilder();

        int newProductsCount = 0;
        List<String> outOfStockProducts = new ArrayList<>();
        for (Product product : products) {
            CartProductId newCartId = new CartProductId(product, cart);
            CartProduct cartItem = cartMap.getOrDefault(newCartId, new CartProduct(newCartId, (short) 0));

            if (cartItem.getQuantity() == 0) {
                newProductsCount++;
            }

            if (cartItem.getCartProductId().getProduct().getQuantityInStock() < cartItem.getQuantity() + 1) {
                if (!stockExceeded) stockExceeded = true;

                outOfStockProducts.add(product.getProductName());
            } else {
                cartItem.setQuantity((short) (cartItem.getQuantity() + 1));
                cartProducts.add(cartItem);
            }
        }

        if (stockExceeded)
        {

            sb.append("Поради недостатъчна или липсваща наличност, ");

            sb.append(outOfStockProducts.size() > 1
                    ? "продуктите: "
                    : "продуктът: ");

            for (int i = 0; i < outOfStockProducts.size(); i++) {
                String separator = switch (outOfStockProducts.size() - (i+1)) {
                    case 0 -> "";
                    case 1 -> " и ";
                    default -> ", ";
                };
                sb.append(outOfStockProducts.get(i)).append(separator);
            }

            if (outOfStockProducts.size() > 1)
                sb.append(" не бяха добавени или увеличени в количката!");
            else sb.append(" не беше добавен или увеличен в количката!");
        }

        if (newProductsCount + originalSize > GlobalConstants.CART_SIZE_LIMIT) {
            throw new CartLimitReachedException("Cart limit reached!");
        }

        try {
            int savedSize = cartProductRepository.saveAll(cartProducts).size();

            if (stockExceeded) {
                if (savedSize == 0)
                    throw new NoStockForCartException(sb.toString());

                return new MessageResponse(ResultTypes.PARTIAL_SUCCESS.getValue(), "Не всички продукти бяха добавени", sb.toString());
            }

            return new MessageResponse(ResultTypes.SUCCESS.getValue(), "", "Успешно добавени в количката!");

        } catch (Exception e) {
            if (e instanceof NoStockForCartException) {
                throw e;
            }

            throw new RuntimeException(e);
        }
    }


    @Transactional
    public List<CartItemResponse> removeBatchFromCartWFetch(Customer customer, List<String> productCodes) {

        Cart cart = cartService.getOrCreateByCustomer(customer);
        cartProductRepository.deleteBatchByCartAndPCodes(cart, productCodes);

//        return cartProductRepository.findDtoByCustomer(customer.getKeycloakId());
        return getCartDtoByCustomer(customer);
    }
    
    @Transactional
    public List<CartItemResponse> removeBatchFromCartWFetch(Session session, List<String> productCodes) {

        Cart cart = sessionCartService.getOrCreateSessionCart(session);
        cartProductRepository.deleteBatchByCartAndPCodes(cart, productCodes);

        return getCartDtoBySession(session);
    }

    public String addQuantityToCart(Product product, short quantity, Customer customer) {

        try
        {
            Cart cart = cartService.getOrCreateByCustomer(customer);
            CartProductId cartId = new CartProductId(product, cart);
            CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);
            return addQuantityToCartMain(product, quantity, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof StockExceededException)
                throw e;
            else
                throw new RuntimeException("-----------------Quantity cart addition for auth user failed-----------------\n " + e.getMessage());
        }
    }

    public String addQuantityToCart(Product product, short quantity, Session session) {

        try
        {
            Cart cart = sessionCartService.getOrCreateSessionCart(session);
            CartProductId cartId = new CartProductId(product, cart);
            CartProduct cartProduct = cartProductRepository.findById(cartId).orElse(null);
            return addQuantityToCartMain(product, quantity, cartId, cartProduct);
        }
        catch (Exception e)
        {
            if (e instanceof StockExceededException)
                throw e;
            else
                throw new RuntimeException("-----------------Quantity cart addition for session failed-----------------\n " + e.getMessage());

        }
    }

    @NonNull
    private String addQuantityToCartMain(Product product, short quantity, CartProductId cartId, CartProduct cartProduct) {
        int quantityInStock = product.getQuantityInStock();

        if (cartProduct != null) {
            short currentCartQuantity = cartProduct.getQuantity();
            if (quantityInStock >= currentCartQuantity + quantity) {
                cartProduct.setQuantity((short) (currentCartQuantity + quantity));
                cartProductRepository.save(cartProduct);
                return "Успешно увеличен в количката!";
            }
            else
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");
        } else {
            if (quantityInStock < quantity)
                throw new StockExceededException("Stock exceeded for product " + product.getProductName() + "!");
            cartProduct = new CartProduct(cartId, quantity);
            cartProductRepository.save(cartProduct);
            return "Успешно добавен в количката!";

        }
    }

    public CartSummaryResponse getSummary(Session session) {

//        Cart cart = sessionCartService.getOrCreateSessionCart(session);

        Cart cart = cartService.getBySessionOptional(session);

        if (cart == null)
        {
            return new CartSummaryResponse(0L, 0L);
        }


       List<CartSummaryItem> cartSummaryItems = cartProductRepository.getSummaryByCartAsItem(cart);

        return ProductDTOMapper.summaryItemsToResponse(cartSummaryItems);

//        return cartProductRepository.getSummaryByCart(cart);
    }

    public CartSummaryResponse getSummary(Customer customer) {

        Cart cart = cartService.getOrCreateByCustomer(customer);

        List<CartSummaryItem> cartSummaryItems = cartProductRepository.getSummaryByCartAsItem(cart);

        return ProductDTOMapper.summaryItemsToResponse(cartSummaryItems);

//        return cartProductRepository.getSummaryByCart(cart);
    }

    @Transactional
    public void deleteItemsBySession(List<Session> sessions) {

        List<String> sessionIds = sessions.stream().map(Session::getSessionId).toList();

        cartProductRepository.deleteCartProductsBySessions(sessionIds);
    }

    @Transactional
    public void mergeCarts(Session session, Customer customer) {
        List<CartProduct> guestCartProducts = cartProductRepository.getBySession(session);

        if (guestCartProducts.isEmpty()) {

            cartService.deleteCartBySession(session.getSessionId());
            return;
        }

        Cart customerCart = cartService.getOrCreateByCustomer(customer);
        List<CartProduct> customerCartProducts = cartProductRepository.findByCustomer(customer.getKeycloakId());

        if (customerCartProducts.isEmpty()) {

            cartService.deleteCartByCustomer(customer);

            Cart sessionCart = sessionCartService.getOrCreateSessionCart(session);
            sessionCart.setCustomer(customer);
            sessionCart.setSession(null);

            cartService.save(sessionCart);
            return;
        }

        List<CartProduct> copiedItems = mergeCartsByComparing(guestCartProducts, customerCartProducts);

        entityManager.flush();

        List<Integer> copiedItemIds = copiedItems
                .stream()
                .map(cp->cp
                        .getCartProductId()
                        .getProduct()
                        .getId())
                .toList();
        Cart sessionCart = sessionCartService.getOrCreateSessionCart(session);

        cartProductRepository.transferNewGuestItemsToCustomer(customerCart,sessionCart, copiedItemIds);
        cartProductRepository.deleteBySessionId(session.getSessionId());
        cartService.deleteCartBySession(session.getSessionId());
    }

    private Integer getProductId(CartProduct cartProduct) {
        return cartProduct.getCartProductId()
                .getProduct()
                .getId();
    }

    private List<CartProduct> mergeCartsByComparing(
            List<CartProduct> guestSessionCartProducts,
            List<CartProduct> customerCartProducts) {

        List<CartProduct> mergedItems = new ArrayList<>();

        Map<Integer, CartProduct> customerProductsMap =
                customerCartProducts.stream()
                        .collect(Collectors.toMap(
                                this::getProductId,
                                cp -> cp
                        ));

        for (CartProduct guestProduct : guestSessionCartProducts) {

            Integer guestProductId =
                    guestProduct.getCartProductId().getProduct().getId();

            CartProduct existingCustomerProduct =
                    customerProductsMap.get(guestProductId);

            if (existingCustomerProduct != null) {

                existingCustomerProduct.setQuantity(
                        (short) (existingCustomerProduct.getQuantity()
                                + guestProduct.getQuantity()));

            } else {

                customerCartProducts.add(guestProduct);
                mergedItems.add(guestProduct);

                customerProductsMap.put(guestProductId, guestProduct);
            }
        }

        return mergedItems;
    }

    public void removeBatchFromCartCustomer(Customer customer, List<String> productCodes) {
        cartProductRepository.deleteBatchByCustomerIdAndCodes(customer.getKeycloakId(), productCodes);
    }

    public void removeBatchFromCartSession(Session session, List<String> productCodes) {
        cartProductRepository.deleteBatchBySessionIdAndCodes(session.getSessionId(), productCodes);
    }
}
