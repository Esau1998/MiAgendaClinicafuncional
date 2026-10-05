package org.esfe.miagendaclinica.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Controla el acceso:
 *  - Paginas publicas: catalogo, login, registro, recuperacion de contrasena y recursos estaticos.
 *  - Todo lo demas exige una sesion iniciada; si no la hay, redirige a /login
 *    y recuerda a que pagina queria entrar.
 *  - /usuarios solo para ADMINISTRADOR.
 *  - Las paginas privadas no se guardan en cache (asi, al cerrar sesion,
 *    el boton "atras" no muestra informacion privada).
 */
@Component
public class SesionFiltro implements Filter {

    private static final Set<String> RUTAS_PUBLICAS = Set.of(
            "/", "/login", "/registro", "/olvide-password", "/restablecer-password",
            "/error", "/favicon.ico", "/catalogo");

    private static final String[] PREFIJOS_PUBLICOS = {
            "/css/", "/js/", "/img/", "/webjars/", "/catalogo/"};

    private static final String[] PREFIJOS_ESTATICOS = {"/css/", "/js/", "/img/", "/webjars/"};

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();

        boolean estatica = empiezaConAlguno(uri, PREFIJOS_ESTATICOS);
        boolean esPublica = RUTAS_PUBLICAS.contains(uri) || empiezaConAlguno(uri, PREFIJOS_PUBLICOS);

        HttpSession session = request.getSession(false);
        boolean autenticado = session != null && session.getAttribute("usuarioId") != null;

        if (!estatica) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        if (esPublica) {
            chain.doFilter(req, res);
            return;
        }

        if (!autenticado) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String destino = uri + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
                request.getSession(true).setAttribute("destino", destino);
            }
            response.sendRedirect("/login");
            return;
        }

        boolean esAdmin = "ADMINISTRADOR".equals(session.getAttribute("rol"));
        if ((uri.equals("/usuarios") || uri.startsWith("/usuarios/")) && !esAdmin) {
            response.sendRedirect("/dashboard");
            return;
        }

        chain.doFilter(req, res);
    }

    private static boolean empiezaConAlguno(String uri, String[] prefijos) {
        for (String p : prefijos) {
            if (uri.startsWith(p)) return true;
        }
        return false;
    }
}
