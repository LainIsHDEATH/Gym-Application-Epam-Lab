package ua.ivan.epam.gym.application.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component("transactionIdFilter")
@Slf4j
public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = resolveTransactionId(request);

        try {
            MDC.put(TRANSACTION_ID, transactionId);
            response.setHeader(TRANSACTION_ID_HEADER, transactionId);

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);

        if (transactionId == null || transactionId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return transactionId;
    }
}
