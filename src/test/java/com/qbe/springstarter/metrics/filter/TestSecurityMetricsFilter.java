package com.qbe.springstarter.metrics.filter;

import static org.mockito.Mockito.*;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.metrics.SampleMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestSecurityMetricsFilter {

    @Mock
    private SampleMetrics sampleMetrics;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private SecurityMetricsFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityMetricsFilter(sampleMetrics);
        lenient().when(request.getContextPath()).thenReturn("");
    }

    @Nested
    @DisplayName("401 Unauthorized")
    class Unauthorized {

        @Test
        @DisplayName("Doit enregistrer un 401 pour create")
        void shouldRecordUnauthorizedForCreate() throws ServletException, IOException {
            configureRequest("POST", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(sampleMetrics).increment(MetricsConstants.CREATE, SampleMetrics.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Doit enregistrer un 401 pour findAll")
        void shouldRecordUnauthorizedForFindAll() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.FIND_ALL, SampleMetrics.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Doit enregistrer un 401 pour findById")
        void shouldRecordUnauthorizedForFindById() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.FIND_BY_ID, SampleMetrics.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Doit enregistrer un 401 pour update")
        void shouldRecordUnauthorizedForUpdate() throws ServletException, IOException {
            configureRequest("PUT", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.UPDATE, SampleMetrics.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Doit enregistrer un 401 pour delete")
        void shouldRecordUnauthorizedForDelete() throws ServletException, IOException {
            configureRequest("DELETE", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.DELETE, SampleMetrics.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("403 Forbidden")
    class Forbidden {

        @Test
        @DisplayName("Doit enregistrer un 403 pour create")
        void shouldRecordForbiddenForCreate() throws ServletException, IOException {
            configureRequest("POST", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.CREATE, SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer un 403 pour findAll")
        void shouldRecordForbiddenForFindAll() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.FIND_ALL, SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer un 403 pour findById")
        void shouldRecordForbiddenForFindById() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.FIND_BY_ID, SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer un 403 pour update")
        void shouldRecordForbiddenForUpdate() throws ServletException, IOException {
            configureRequest("PUT", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.UPDATE, SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer un 403 pour delete")
        void shouldRecordForbiddenForDelete() throws ServletException, IOException {
            configureRequest("DELETE", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.DELETE, SampleMetrics.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Non security errors")
    class NonSecurityErrors {

        @Test
        @DisplayName("Ne doit rien enregistrer pour un 200")
        void shouldIgnoreSuccessfulResponse() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(sampleMetrics, never())
                    .increment(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Ne doit rien enregistrer pour un 404")
        void shouldIgnoreNotFoundResponse() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_NOT_FOUND);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics, never())
                    .increment(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Ne doit rien enregistrer pour un 500")
        void shouldIgnoreInternalServerError() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics, never())
                    .increment(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        }
    }

    @Nested
    @DisplayName("Unknown operations")
    class UnknownOperations {

        @Test
        @DisplayName("Doit enregistrer unknown pour une route inconnue retournant 401")
        void shouldRecordUnknownForUnknownPath() throws ServletException, IOException {
            configureRequest("GET", "/v1/unknown");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment("unknown", SampleMetrics.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Doit enregistrer unknown pour une méthode non supportée sur la collection")
        void shouldRecordUnknownForUnsupportedCollectionMethod() throws ServletException, IOException {
            configureRequest("PATCH", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment("unknown", SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer unknown pour une méthode non supportée sur une ressource")
        void shouldRecordUnknownForUnsupportedResourceMethod() throws ServletException, IOException {
            configureRequest("POST", "/api/v1/samples/123");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment("unknown", SampleMetrics.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Context path")
    class ContextPath {

        @Test
        @DisplayName("Doit retirer le context path avant de résoudre l'opération")
        void shouldRemoveContextPath() throws ServletException, IOException {
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/springstarter/api/v1/samples/123");
            when(request.getContextPath()).thenReturn("/springstarter");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            filter.doFilterInternal(request, response, filterChain);

            verify(sampleMetrics).increment(MetricsConstants.FIND_BY_ID, SampleMetrics.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Filter chain")
    class FilterChainExecution {

        @Test
        @DisplayName("Doit toujours exécuter la chaîne de filtres")
        void shouldExecuteFilterChain() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");
            when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Doit enregistrer le statut même si la chaîne lève une ServletException")
        void shouldRecordStatusWhenServletExceptionOccurs() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples/123");

            ServletException exception = new ServletException("Security error");

            org.mockito.Mockito.doThrow(exception).when(filterChain).doFilter(request, response);

            when(response.getStatus()).thenReturn(HttpServletResponse.SC_FORBIDDEN);

            org.junit.jupiter.api.Assertions.assertThrows(
                    ServletException.class, () -> filter.doFilterInternal(request, response, filterChain));

            verify(sampleMetrics).increment(MetricsConstants.FIND_BY_ID, SampleMetrics.FORBIDDEN);
        }

        @Test
        @DisplayName("Doit enregistrer le statut même si la chaîne lève une IOException")
        void shouldRecordStatusWhenIOExceptionOccurs() throws ServletException, IOException {
            configureRequest("GET", "/api/v1/samples");

            IOException exception = new IOException("I/O error");

            org.mockito.Mockito.doThrow(exception).when(filterChain).doFilter(request, response);

            when(response.getStatus()).thenReturn(HttpServletResponse.SC_UNAUTHORIZED);

            org.junit.jupiter.api.Assertions.assertThrows(
                    IOException.class, () -> filter.doFilterInternal(request, response, filterChain));

            verify(sampleMetrics).increment(MetricsConstants.FIND_ALL, SampleMetrics.UNAUTHORIZED);
        }
    }

    private void configureRequest(String method, String uri) {
        lenient().when(request.getMethod()).thenReturn(method);
        lenient().when(request.getRequestURI()).thenReturn(uri);
    }
}
