package com.example.ecomerseapplication.ExceptionHandling.GlobalHandler;

import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {

        ErrorResponse error = new ErrorResponse(
                ErrorType.VALIDATION_ERROR,
                "Невалидни данни",
                400,
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(UserAlreadyExistsException ex) {

        HttpStatus errorStatus = HttpStatus.CONFLICT;

        ErrorResponse errorResponse = new ErrorResponse(ErrorType.USER_ALREADY_EXISTS,
                "Съществуващ потребител",
                errorStatus.value(),
                ex.getMessage());
        return ResponseEntity.status(errorStatus).body(errorResponse);
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationFailedExceptions() {

        ErrorResponse errorResponse = new ErrorResponse(ErrorType.REGISTRATION_FAILED,
                "Неуспешна регистрация",
                400,
                "");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<?> handleAllExceptions() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(RefreshRequestFailedException.class)
    public ResponseEntity<?> handleAllRefreshExceptions() {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(FavouriteInsertFailedException.class)
    public ResponseEntity<?> handleGenericFavouriteExceptions() {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(FavouriteSizeLimitReachedException.class)
    public ResponseEntity<?> handleFavouriteLimitReachedExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                "Достигнат лимит на любими",
                HttpStatus.CONFLICT.value(), "Достигнахте максималният лимит на списъка с любими!"));
    }

    @ExceptionHandler(ProductAlreadyInFavouritesException.class)
    public ResponseEntity<?> handleFavouriteDuplicationExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                "Продуктът вече е в любими",
                HttpStatus.CONFLICT.value(), "Избраният продукт вече е в списъка ви с любими!"));
    }

    @ExceptionHandler(EntityDeletionFailedException.class)
    public ResponseEntity<?> handleDeletionFailedExceptions() {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(EmptyRequestException.class)
    public ResponseEntity<?> handleEmptyRequestExceptions() {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(CartLimitReachedException.class)
    public ResponseEntity<?> handleCartLimitReachedExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                "Неуспешно добавяне на продукт/и",
                HttpStatus.CONFLICT.value(),
                "Достигнахте лимита на количката!"));
    }

    @ExceptionHandler(StockExceededException.class)
    public ResponseEntity<?> handleStockExceededExceptions() {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.DEMAND_EXCEEDS_SUPPLY,
                "Неуспешно увеличение на бройка",
                HttpStatus.CONFLICT.value(),
                "Изисканото количество надхвърля наличното за този продукт, и той не бе добавен или увеличен в количката!"));
    }

    @ExceptionHandler(NoStockForCartException.class)
    public ResponseEntity<?> handleNoStockForCartExceptions(NoStockForCartException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.NO_DATA_FOR_QUERY,
                "Продуктите не бяха добавени",
                HttpStatus.CONFLICT.value()
                , ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundExceptions(ResourceNotFoundException ex) {
        System.out.println("--------------------Resource not found-----------------");
        System.out.println(ex.getMessage());
        System.out.println("-------------------------------------------------------");
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler(ReviewSoftDeletedException.class)
    public ResponseEntity<?> handleReviewSoftDeleteExceptions() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                "Не може да се добавят повече ревюта",
                HttpStatus.FORBIDDEN.value(),
                "Не можете да добавяте повече ревюта за този продукт"));
    }

    @ExceptionHandler(PostOrUpdateReviewForbiddenException.class)
    public ResponseEntity<?> handlePostOrUpdateReviewForbiddenExceptions() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                "Не може да се добавят повече ревюта",
                HttpStatus.FORBIDDEN.value(),
                "Вече сте добавили ревю за този продукт. Срокът за редакция е изтекъл и не могат да се правят промени."));
    }

    @ExceptionHandler(IncorrectRatingException.class)
    public ResponseEntity<?> handleIncorrectRatingException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Само стойности от 1-5 са позволени!");
    }

    @ExceptionHandler(ReviewTextLimitReachedException.class)
    public ResponseEntity<?> handleReviewTextLimitReachedExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.SIZE_LIMIT_REACHED,
                "Надвишен лимит",
                HttpStatus.BAD_REQUEST.value(),
                "Размера на коментара надвишава максималният размер"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationExceptions() {

        System.out.println("--------------------Entity or Database constraint violation-----------------");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<?> handleInvalidSessionExceptions() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UserIdExtractException.class)
    public ResponseEntity<?> handleUserIdExtractExceptions() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(StockForNamedProductExceeded.class)
    public ResponseEntity<?> handleStockForNamedProductExceededExceptions(StockForNamedProductExceeded ex) {
        String productName = ex.getProductName();
        int quantityInStock = ex.getQuantity();
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorType.DEMAND_EXCEEDS_SUPPLY,
                "Неналично количество за продукт",
                HttpStatus.BAD_REQUEST.value(),
                "Изисканото количество надхвърля наличното за продукта: " + productName
                        + "\nНалично количество: " + quantityInStock + "бр."
        ));
    }

    @ExceptionHandler(PessimisticLockOrTimeoutPurchaseException.class)
    public ResponseEntity<?> handleProductLockOrTimeoutExceptionsForPurchase(PessimisticLockOrTimeoutPurchaseException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                ErrorType.RESOURCE_CONFLICT,
                exception.getTitle(),
                HttpStatus.CONFLICT.value()
                , exception.getDetail()
        ));
    }

    @ExceptionHandler(NoCategoryAndManufacturerPresentException.class)
    public ResponseEntity<?> handleNoCategoryAndManufacturerPresentExceptions() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<?> handleInvalidImageExceptions(InvalidImageException ex) {
        String title = "", message = "";

        switch (ex.getType()) {
            case "empty" -> {
                title = "Не открита снимка";
                message = "Заявката трябвада съдържа снимка!";
            }
            case "contentType" -> {
                title = "Неправилен тип на съдържание";
                message = "Неподходящ тип на изпратеното съдържание!";
            }
            case "decoding" -> {
                title = "Невалиден файл";
                message = "Изпратеният файл е невалиден!";
            }
            case "size-small" -> {
                title = "Недостатъчен размер";
                message = "Изпратеното изображение е прекалено малко!";
            }
            case "size-large" -> {
                title = "Ограничение за размер";
                message = "Изпратеното изображение е прекалено голямо!";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(ErrorType.VALIDATION_ERROR
                , title,
                HttpStatus.BAD_REQUEST.value(),
                message);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(BadPurchaseCancelRequestException.class)
    public ResponseEntity<?> handleBadOrderCancelRequestExceptions(BadPurchaseCancelRequestException exception) {
        System.out.println("--------------------Bad order cancel request-----------------");
        System.out.println(exception.getMessage());
        System.out.println("-------------------------------------------------------");
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }


    @ExceptionHandler(BadPurchaseRefundRequestException.class)
    public ResponseEntity<?> handleBadOrderCancelRequestExceptions(BadPurchaseRefundRequestException exception) {
        System.out.println("--------------------Bad order refund request-----------------");
        System.out.println(exception.getMessage());
        System.out.println("-------------------------------------------------------");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                exception.getTitle(),
                HttpStatus.BAD_REQUEST.value(),
                exception.getDetail()));
    }

    @ExceptionHandler(ProductAlreadyInSaleException.class)
    public ResponseEntity<?> handleProductAlreadyInSaleExceptions(ProductAlreadyInSaleException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.RESOURCE_CONFLICT,
                "Продуктът е зает",
                HttpStatus.CONFLICT.value(),
                "Продуктът " + exception.getProductName() + " вече е част от промоцията: " + exception.getSaleName()));
    }

    @ExceptionHandler(EmptyAttributeValueException.class)
    public ResponseEntity<?> handleEmptyAttributeValueExceptions() {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                        "Невалиден атрибут",
                        HttpStatus.BAD_REQUEST.value(),
                        "Един или повече от подадените атрибути нямат въведена стойност!"));
    }

    @ExceptionHandler(DuplicatedAttributeException.class)
    public ResponseEntity<?> handleDuplicatedAttributeExceptions() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                        "Дублиране на атрибути",
                        HttpStatus.CONFLICT.value(),
                        "Има дублиране при един или повече от подадените атрибути"));
    }

    @ExceptionHandler(InvalidEnumNameException.class)
    public ResponseEntity<?> handleInvalidEnumNameExceptions(InvalidEnumNameException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                        exception.getTitle(),
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getDetail()));
    }

    @ExceptionHandler(InvalidPurchaseActionException.class)
    public ResponseEntity<?> handleInvalidPurchaseActionException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorType.INVALID_OPERATION,
                        "Невалидно действие",
                        HttpStatus.BAD_REQUEST.value(),
                        "Това действие не може да се изпълни за тази поръчка"));
    }
}
