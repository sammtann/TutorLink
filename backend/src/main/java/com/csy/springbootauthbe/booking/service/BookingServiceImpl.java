package com.csy.springbootauthbe.booking.service;

import com.csy.springbootauthbe.booking.dto.BookingDTO;
import com.csy.springbootauthbe.booking.dto.BookingRequest;
import com.csy.springbootauthbe.booking.dto.RecentBookingResponse;
import com.csy.springbootauthbe.booking.entity.Booking;
import com.csy.springbootauthbe.booking.mapper.BookingMapper;
import com.csy.springbootauthbe.booking.observer.BookingEvent;
import com.csy.springbootauthbe.booking.observer.BookingNotificationObserver;
import com.csy.springbootauthbe.booking.observer.BookingObserver;
import com.csy.springbootauthbe.booking.repository.BookingRepository;
import com.csy.springbootauthbe.common.utils.SanitizedLogger;
import com.csy.springbootauthbe.notification.service.NotificationService;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import com.csy.springbootauthbe.wallet.service.WalletService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private static final SanitizedLogger logger = SanitizedLogger.getLogger(BookingServiceImpl.class);
    private final WalletService walletService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final List<BookingObserver> observers = new ArrayList<>();

    private void notifyObservers(Booking booking, BookingEvent.Type type, String initiatorUserId) {
        BookingEvent event = new BookingEvent(booking, type, initiatorUserId);
        for (BookingObserver observer : observers) {
            observer.handleBookingEvent(event);
        }
    }
    public void addObserver(BookingObserver observer) {
        observers.add(observer);
    }

    @PostConstruct
    private void initObservers() {
        this.addObserver(new BookingNotificationObserver(notificationService));
    }

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequest dto) {
        logger.info("Creating booking with payload: {}", dto);
        // 1️⃣ Overlap check (same tutor)
        List<Booking> existing = bookingRepository.findByTutorIdAndDate(dto.getTutorId(), dto.getDate());
        boolean overlap = existing.stream()
                .filter(b -> List.of("pending", "confirmed", "on_hold").contains(b.getStatus()))
                .anyMatch(b -> b.getStart().compareTo(dto.getEnd()) < 0 && dto.getStart().compareTo(b.getEnd()) < 0);
        if (overlap) {
            throw new RuntimeException("Selected slot is already booked.");
        }

        // 2️⃣ Check if student already booked with another tutor on same day
        List<Booking> studentSameDay = bookingRepository.findByStudentIdAndDate(dto.getStudentId(), dto.getDate());
        boolean conflict = studentSameDay.stream()
                .filter(b -> List.of("pending", "confirmed", "on_hold").contains(b.getStatus()))
                .anyMatch(b -> !b.getTutorId().equals(dto.getTutorId()));
        if (conflict) {
            throw new RuntimeException("You already have a booking with another tutor on this date.");
        }

        // 3️⃣ Validate amount
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid booking amount");
        }

        // 4️⃣ Hold student credits
        walletService.holdCredits(dto.getStudentId(), dto.getAmount(), "BOOKING-" + UUID.randomUUID());

        // 5️⃣ Save booking
        Booking booking = bookingMapper.toEntity(dto);
        booking.setTutorName(dto.getTutorName());
        booking.setStudentName(dto.getStudentName());
        booking.setStatus("pending");
        booking.setAmount(dto.getAmount());
        Booking saved = bookingRepository.save(booking);

        // notify
        notifyObservers(saved, BookingEvent.Type.booking_created, saved.getStudentId()); // student initiates booking creation

        return bookingMapper.toDto(saved);
    }



    @Override
    public RecentBookingResponse getRecentPastBookings(String tutorId) {
        logger.info("Get Recenting booking with tutor: {}", tutorId);
        String todayStr = LocalDate.now().format(formatter);
        String status = "confirmed";
        long totalCompleted = bookingRepository
                .countByTutorIdAndStatusAndDateBefore(tutorId, status, todayStr);

        List<Booking> recentPastSessions = bookingRepository
                .findTop5ByTutorIdAndStatusAndDateBeforeOrderByDateDesc(
                        tutorId, status, todayStr);

        RecentBookingResponse response = new RecentBookingResponse();
        response.setRecentSessions(recentPastSessions.stream().map(bookingMapper::toDto).toList());
        response.setTotalCount(totalCompleted);

        return response;
    }

    @Override
    public RecentBookingResponse getUpcomingBookings(String tutorId) {
        logger.info("Getting upcoming 5 bookings for a tutor : {}", tutorId);
        String todayStr = LocalDate.now().format(formatter);
        List<String> statuses = List.of("confirmed", "pending","on_hold");

        List<Booking> upcomingSessions = bookingRepository
                .findByTutorIdAndStatusInAndDateGreaterThanEqualOrderByDateAsc(
                        tutorId, statuses, todayStr);

        // limit to 5 upcoming sessions
        List<Booking> next5Sessions = upcomingSessions.stream()
                .limit(5)
                .toList();
        RecentBookingResponse response = new RecentBookingResponse();
        response.setRecentSessions(next5Sessions.stream().map(bookingMapper::toDto).toList());
        response.setTotalCount(upcomingSessions.size());
        return response;
    }



    @Override
    public List<BookingDTO> getBookingsForTutor(String tutorId, String date) {
        logger.info("Fetching bookings for a tutor : {}", tutorId);
        return bookingRepository.findByTutorIdAndDate(tutorId, date)
                .stream().map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsForTutorBetweenDates(String tutorId, String startDate, String endDate) {
        logger.info("Fetching bookings for tutorId={} between {} and {}", tutorId, startDate, endDate);

        List<Booking> bookings = bookingRepository.findBookingsByTutorIdAndDateRange(tutorId, startDate, endDate);

        logger.info("Found {} bookings", bookings.size());
        for (Booking b : bookings) {
            logger.info("Booking: id={}, date={}, status={}", b.getId(), b.getDate(), b.getStatus());
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<BookingDTO> getBookingsForStudent(String studentId) {
        logger.info("Get booking with payload student: {}", studentId);
        List<Booking> bookings = bookingRepository.findByStudentId(studentId);

        // Sort by date ascending, then start time ascending
        bookings.sort(Comparator
                .comparing(Booking::getDate)                 // assuming getDate() returns java.time.LocalDate or java.util.Date
                .thenComparing(Booking::getStart)           // assuming getStart() returns java.time.LocalTime or String "HH:mm"
        );

        // Get all unique userIds
        Set<String> userIds = bookings.stream()
                .flatMap(b -> Stream.of(b.getStudentId(), b.getTutorId()))
                .collect(Collectors.toSet());

        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return bookings.stream().map(booking -> {
            BookingDTO dto = bookingMapper.toDto(booking);

            User student = usersMap.get(booking.getStudentId());
            User tutor = usersMap.get(booking.getTutorId());

            dto.setStudentName(student.getFirstname() + " " + student.getLastname());
            dto.setTutorName(tutor.getFirstname() + " " + tutor.getLastname());

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public BookingDTO acceptBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only allow acceptance of pending bookings
        if (!"pending".equals(booking.getStatus())) {
            throw new RuntimeException("Only pending bookings can be accepted.");
        }

        booking.setStatus("confirmed");
        Booking savedBooking = bookingRepository.save(booking);

        // ✅ Release funds from student to tutor
        if (booking.getAmount() != null && booking.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.releaseToTutor(
                    booking.getStudentId(),
                    booking.getTutorId(),
                    booking.getAmount(),
                    booking.getId()
            );
        }

        // Notify
        notifyObservers(booking, BookingEvent.Type.booking_accepted, booking.getTutorId()); // tutor initiates accept booking

        return bookingMapper.toDto(savedBooking);
    }



    // Only Tutor can accept booking
    @Override
    @Transactional
    public BookingDTO cancelBooking(String bookingId, String currentUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only allow cancel if not already confirmed or cancelled
        if (List.of("cancelled", "completed").contains(booking.getStatus())) {
            throw new RuntimeException("Booking is already " + booking.getStatus());
        }

        // Determine if refund is needed
        boolean refundable = "pending".equals(booking.getStatus()) || "on_hold".equals(booking.getStatus());

        booking.setStatus("cancelled");
        Booking savedBooking = bookingRepository.save(booking);

        // ✅ Refund student if booking not yet accepted
        if (refundable && booking.getAmount() != null && booking.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.refundStudent(
                    booking.getStudentId(),
                    booking.getAmount(),
                    booking.getId()
            );
        }

        // ✅ Notify the other user
        notifyObservers(savedBooking, BookingEvent.Type.booking_cancelled, currentUserId);

        return bookingMapper.toDto(savedBooking);
    }



    @Override
    public BookingDTO getBookingById(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return bookingMapper.toDto(booking);
    }

    @Transactional
    @Override
    public BookingDTO requestReschedule(String bookingId, BookingRequest newSlotRequest) {
        logger.info("Requesting reschedule for bookingId={} with payload: {}", bookingId, newSlotRequest);

        // 1. Fetch current booking
        Booking currentBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    logger.error("Booking not found for id={}", bookingId);
                    return new RuntimeException("Booking not found");
                });
        logger.info("Current booking fetched: {}", currentBooking);

        // 2. Ensure booking is confirmed
        if (!"confirmed".equals(currentBooking.getStatus())) {
            logger.warn("Booking status is not confirmed: {}", currentBooking.getStatus());
            throw new RuntimeException("Only confirmed bookings can be rescheduled.");
        }

        // 3. Check for overlap on new slot
        List<Booking> overlapping = bookingRepository.findByTutorIdAndDate(newSlotRequest.getTutorId(), newSlotRequest.getDate());
        logger.info("Found {} bookings on the same date for tutorId={}", overlapping.size(), newSlotRequest.getTutorId());

        boolean conflict = overlapping.stream()
                .filter(b -> "pending".equals(b.getStatus()) || "confirmed".equals(b.getStatus()) || "on_hold".equals(b.getStatus()))
                .anyMatch(b -> b.getStart().compareTo(newSlotRequest.getEnd()) < 0 &&
                        newSlotRequest.getStart().compareTo(b.getEnd()) < 0);

        if (conflict) {
            logger.warn("Conflict detected for new slot: start={}, end={}", newSlotRequest.getStart(), newSlotRequest.getEnd());
            throw new RuntimeException("Selected slot is already booked.");
        }

        // 4. Update current booking status to RESCHEDULE_REQUESTED
        currentBooking.setStatus("reschedule_requested");
        bookingRepository.save(currentBooking);
        logger.info("Updated current booking to reschedule_requested: {}", currentBooking.getId());

        // 5. Create a new booking in ON_HOLD for the requested slot
        Booking newBooking = bookingMapper.toEntity(newSlotRequest);
        newBooking.setStatus("on_hold");
        newBooking.setOriginalBookingId(currentBooking.getId());
        Booking savedNewBooking = bookingRepository.save(newBooking);
        logger.info("Created new on_hold booking: {}", savedNewBooking.getId());

        // 6. Notify tutor
        notifyObservers(savedNewBooking, BookingEvent.Type.reschedule_requested, savedNewBooking.getStudentId()); // student requests reschedule
        logger.info("Notification sent to tutorId={}", currentBooking.getTutorId());

        return bookingMapper.toDto(savedNewBooking);
    }


    @Transactional
    @Override
    public BookingDTO approveReschedule(String newBookingId) {
        // 1. Fetch new booking
        Booking newBooking = bookingRepository.findById(newBookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 2. Fetch current booking
        Booking currentBooking = bookingRepository.findById(newBooking.getOriginalBookingId())
                .orElseThrow(() -> new RuntimeException("Original booking not found"));

        // 3. Update current booking → CANCELLED
        currentBooking.setStatus("cancelled");
        bookingRepository.save(currentBooking);

        // 4. Update new booking → CONFIRMED
        newBooking.setStatus("confirmed");
        Booking savedNewBooking = bookingRepository.save(newBooking);

        // 5. Notify
        notifyObservers(savedNewBooking, BookingEvent.Type.reschedule_requested, savedNewBooking.getTutorId()); // tutor initiates accept reschedule

        return bookingMapper.toDto(savedNewBooking);
    }

    @Transactional
    @Override
    public BookingDTO rejectReschedule(String newBookingId) {
        // 1. Fetch new (on_hold) booking
        Booking newBooking = bookingRepository.findById(newBookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!"on_hold".equals(newBooking.getStatus())) {
            throw new RuntimeException("Only on_hold bookings can be rejected.");
        }

        // 2. Fetch the original booking
        Booking originalBooking = bookingRepository.findById(newBooking.getOriginalBookingId())
            .orElseThrow(() -> new RuntimeException("Original booking not found"));

        // 3. Restore original booking status to confirmed
        originalBooking.setStatus("confirmed");
        bookingRepository.save(originalBooking);

        // 4. Cancel the new on_hold booking
        newBooking.setStatus("cancelled");
        bookingRepository.save(newBooking);

        // 5. Notify student and tutor
        notificationService.createNotification(
            newBooking.getStudentId(),
            "reschedule_rejected",
            newBooking.getId(),
            "Your reschedule request of " + newBooking.getDate() + " for " + newBooking.getLessonType() +
                " from " + newBooking.getTutorName() + " was rejected. Your original booking remains confirmed."
        );

        notificationService.createNotification(
            newBooking.getTutorId(),
            "reschedule_rejected",
            newBooking.getId(),
            "You have rejected the reschedule request " + newBooking.getDate() + " " + newBooking.getLessonType() +
                " for " + newBooking.getStudentName() + ". The original booking has been restored."
        );

        return bookingMapper.toDto(originalBooking);
    }

    @Override
    public RecentBookingResponse getPastSessionsForStudent(String studentId) {
        String todayStr = LocalDate.now().format(formatter);
        List<String> completedStatuses = List.of("completed", "confirmed");

        // 1 Fetch all past sessions before today
        List<Booking> pastSessions = bookingRepository
                .findByStudentIdAndStatusInAndDateBeforeOrderByDateDesc(studentId, completedStatuses, todayStr);

        long totalCount = pastSessions.size();

        // 2 Collect all tutor IDs
        Set<String> tutorIds = pastSessions.stream()
                .map(Booking::getTutorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3 Fetch tutor user info
        Map<String, User> tutorMap = userRepository.findAllById(tutorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4 Map bookings to DTOs with tutor name
        List<BookingDTO> dtoList = pastSessions.stream().map(b -> {
            BookingDTO dto = bookingMapper.toDto(b);

            User tutor = tutorMap.get(b.getTutorId());
            if (tutor != null) {
                dto.setTutorName(tutor.getFirstname() + " " + tutor.getLastname());
            } else {
                dto.setTutorName("Unknown Tutor");
            }

            return dto;
        }).toList();

        // 5 Build response
        RecentBookingResponse response = new RecentBookingResponse();
        response.setRecentSessions(dtoList);
        response.setTotalCount(totalCount);

        return response;
    }

    @Override
    public BookingDTO deleteBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("cancelled");
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return bookingMapper.toDto(booking);
    }


}
