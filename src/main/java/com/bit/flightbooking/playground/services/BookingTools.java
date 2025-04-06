package com.bit.flightbooking.playground.services;

import com.bit.flightbooking.playground.data.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.function.Function;

@Service
public class BookingTools {

    private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

    @Autowired
    private FlightBookingService flightBookingService;

    public record BookingDetailsRequest(@ToolParam(description = "预定号") String bookingNumber,
                                        @ToolParam(description = "用户名") String name) {
    }

    public record ChangeBookingDatesRequest(String bookingNumber, String name, String date, String from, String to) {
    }

    public record CancelBookingRequest(String bookingNumber, String name) {
    }

    @JsonInclude(Include.NON_NULL)
    public record BookingDetails(String bookingNumber, String name, LocalDate date, BookingStatus bookingStatus,
                                 String from, String to, String bookingClass) {
    }

    @Tool(description = "获取机票预定详细信息")
    public BookingDetails getBookingDetails(@ToolParam(description = "获取机票预定详细信息参数") BookingDetailsRequest request) {
        try {
            return flightBookingService.getBookingDetails(request.bookingNumber(), request.name());
        } catch (Exception e) {
            logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return new BookingDetails(request.bookingNumber(), request.name(), null, null, null, null, null);

        }
    }

    @Tool(description = "修改机票预定日期")
    public String changeBooking(ChangeBookingDatesRequest request) {

        flightBookingService.changeBooking(request.bookingNumber(), request.name(), request.date(), request.from(),
                request.to());
        return "修改成功";
    }

    @Tool(description = "取消机票预定")
    public String cancelBooking(CancelBookingRequest request) {
        flightBookingService.cancelBooking(request.bookingNumber(), request.name());
        return "取消机票预定成功";

    }

}
