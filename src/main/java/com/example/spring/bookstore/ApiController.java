//package com.example.spring.bookstore;
//
//import com.example.spring.bookstore.db.book.Book;
//import com.example.spring.bookstore.db.book.BookRepository;
//import com.example.spring.bookstore.db.order.Order;
//import com.example.spring.bookstore.db.user.User;
//import com.example.spring.bookstore.db.user.UserRepository;
//import com.example.spring.bookstore.db.visit.Visit;
//import com.example.spring.bookstore.db.visit.VisitRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletResponse;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api")
//public class ApiController {
//
//    final Logger log = LoggerFactory.getLogger(ApiController.class);
//
//    private final VisitRepository visitRepository;
//    private final BookRepository bookRepository;
//    private final UserRepository userRepository;
//
//    @Autowired
//    public ApiController(VisitRepository visitRepository, BookRepository bookRepository, UserRepository userRepository) {
//        this.visitRepository = visitRepository;
//        this.bookRepository = bookRepository;
//        this.userRepository = userRepository;
//        fillBookRepo();
//        fillUserRepo();
//    }
//
//    private void fillBookRepo() {
//        for (int i = 1; i <= 10; i++) {
//            Book book = new Book();
//            book.setName("Book " + i);
//            book.setCount(1);
//            bookRepository.save(book);
//        }
//    }
//
//    private void fillUserRepo() {
//        ArrayList<User> users = new ArrayList<>();
//        users.add(new User("Yuri"));
//        users.add(new User("Ivan"));
//        for (User user : users) {
//            if (user != null) {
//                userRepository.save(user);
//            }
//        }
//    }
//
//    private boolean isUserExists(String name) {
//        Optional<User> user = userRepository.findByName(name);
//        if (user.isPresent()) {
//            log.info("User is present");
//        }
//        return user.isPresent();
//    }
//
//    @GetMapping("/visit")
//    public String visit() {
//        Visit visit = new Visit();
//        String visitString = String.format("Visited at %s", LocalDateTime.now());
//        visit.setDescription(visitString);
//        visitRepository.save(visit);
//        return "Visited";
//    }
//
//
//    @GetMapping("/get-visits")
//    public Iterable<Visit> getVisits() {
//        return visitRepository.findAll();
//    }
//
//    @GetMapping("/books")
//    public Iterable<Book> getBooks() {
//        return bookRepository.findAll();
//    }
//
//    @GetMapping("/users")
//    public Iterable<User> getUsers() {
//        return userRepository.findAll();
//    }
//
//    @GetMapping(path = "/order", produces = "application/json")
//    public Order order(HttpServletResponse response) {
//        Order newOrder = new Order();
//        newOrder.setOrderId(132L);
//        newOrder.setUserId(12L);
//        newOrder.setStatus(Order.Status.PENDING);
//        newOrder.setTotalPayment(1200);
//
//        ArrayList<Long> allBookIds = new ArrayList<>();
//        bookRepository.findAll().forEach((book -> allBookIds.add(book.getId())));
//        newOrder.setBooks(allBookIds);
//
//        response.setStatus(HttpServletResponse.SC_ACCEPTED);
//        return newOrder;
//    }
//
//    @PostMapping(value = "/new-order/{bookIds}/{userName}", produces = "application/json")
//    public Order newOrder(@PathVariable ArrayList<Long> bookIds,
//                          @PathVariable String userName,
//                          HttpServletResponse response) {
//        log.info("Post!");
//        if (!canWeSellThisBooksToUser(bookIds, userName)) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            return null;
//        }
//        Order order = new Order();
//        order.setTotalPayment(100f);
//        order.setStatus(Order.Status.PENDING);
//        order.setUserId(1L);
//        order.setOrderId(33L);
//
//        order.setBooks(bookIds);
//
//        return order;
//    }
//
//    private Boolean canWeSellThisBooksToUser(List<Long> bookIds, String userName) {
//        for (Long bookId : bookIds) {
//            if (getBooksCount(bookId) <= 0) {
//                return false;
//            }
//        }
//
//        if (!isUserExists(userName)) {
//            return false;
//        }
//        log.info("We can!");
//        return true;
//    }
//
//    private int getBooksCount(Long id) {
//        Optional<Book> book = bookRepository.findById(id);
//        if (book.isPresent()) {
//            log.info("BookIs: {} BookCount: {}", book.get().getId(), book.get().getCount());
//            return book.get().getCount();
//        }
//        return 0;
//    }
//
//}
