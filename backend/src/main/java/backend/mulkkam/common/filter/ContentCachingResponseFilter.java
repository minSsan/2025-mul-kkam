package backend.mulkkam.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class ContentCachingResponseFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException
    {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(res);
        try {
            chain.doFilter(req, wrapper);
        } finally {
            /* 캐싱한 응답을 클라이언트에게 flush */
            wrapper.copyBodyToResponse();
        }
    }
}
