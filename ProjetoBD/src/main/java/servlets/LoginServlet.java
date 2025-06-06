package servlets;

import java.io.IOException;
import java.io.Serial;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.CargoDao;
import dao.CriarSolicitacaoDao;
import dao.SolicitacaoDao;
import model.SolicitacaoModel;


@WebServlet(urlPatterns = {"/LoginServlet", "/SolicitanteServlet"})
public class LoginServlet extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;
       
    
	String email = "www";
	String password = "www";

    public LoginServlet() {
       
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 // Busca email e senha da sessão
        String email = (String) request.getSession().getAttribute("email");
        String password = (String) request.getSession().getAttribute("password");
        if (email == null || password == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        SolicitacaoDao solicitacao = new SolicitacaoDao();
        List<SolicitacaoModel> solicitacoes;
        try {
            solicitacoes = solicitacao.listarSolicitacoesPorUsuario(email, password);
            if(solicitacoes.isEmpty()) {
                request.setAttribute("Erro", "Nenhuma solicitação cadastrada" );
            }
            request.setAttribute("lista", solicitacoes);
            dao.EspacoDao espacoDao = new dao.EspacoDao();
            List<model.EspacoModel> espacos = espacoDao.listar();
            request.setAttribute("espacos", espacos);
            request.getRequestDispatcher("telaSolicitante.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String acao = request.getServletPath();

    switch (acao) {
        case "/LoginServlet":
            handleLogin(request, response);
            break;
        case "/SolicitanteServlet":
            try {
                handleSolicitante(request, response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            break;
        default:
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ação não reconhecida");
    }
		}
	
	
	private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    CargoDao dao = new CargoDao();
    String cargo = dao.verificaCargo(email, password);

    // Salva email e senha na sessão
    request.getSession().setAttribute("email", email);
    request.getSession().setAttribute("password", password);

    if ("GESTOR".equals(cargo)) {
        RequestDispatcher redirecionar = request.getRequestDispatcher("telaPrincipal.jsp");
        redirecionar.forward(request, response);
    } else if ("SOLICITANTE".equals(cargo)) {
        
        SolicitacaoDao solicitacaoDao = new SolicitacaoDao();
        List<SolicitacaoModel> solicitacoes = null;
        try {
            solicitacoes = solicitacaoDao.listarSolicitacoesPorUsuario(email, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("lista", solicitacoes);

        dao.EspacoDao espacoDao = new dao.EspacoDao();
        List<model.EspacoModel> espacos = null;
        try {
            espacos = espacoDao.listar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("espacos", espacos);
        RequestDispatcher redirecionar = request.getRequestDispatcher("telaSolicitante.jsp");
        redirecionar.forward(request, response);
    } else {
        request.setAttribute("erroLogin", "Usuário ou senha inválidos");
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }
}

private void handleSolicitante(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ParseException, SQLException {
    String email = (String) request.getSession().getAttribute("email");
    String password = (String) request.getSession().getAttribute("password");

    String dataReserva = request.getParameter("data");
    String horarioInicio = request.getParameter("horarioInicio");
    String horarioFim = request.getParameter("horarioFim");
    String nomeEspaco = request.getParameter("sala");

    CriarSolicitacaoDao dao = new CriarSolicitacaoDao();
    dao.cadastrarSolicitacao(email, password, dataReserva, horarioInicio, horarioFim, nomeEspaco);
    dao.cadastraAuditoria(email, password);

    
}

	
}
