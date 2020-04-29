package cs308.sabanciuniv.edu;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet implementation class OrderServlet
 */
@WebServlet(name = "placeorder", urlPatterns = { "/placeorder" })
public class OrderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OrderServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("In do post of order servlet!!!!");
		try
		{			
			HttpSession session = request.getSession();
			User user = (User)session.getAttribute("user");
			if(user == null)
			{
				System.out.println("Compile test.");
				session.setAttribute("order-error", "Please login before placing an order!!!!");
				response.setHeader("order-error","true");
				return;
			}
			else{
				ResultSet rs = null;
				//String userEmail = user.getEmail();
				String[] itemNames = request.getParameter("list_names").split(",");
				String[] itemQuantities = request.getParameter("list_q").split(",");
				Map<Games, Integer> hashmap = new HashMap<>();
				EntityManagerFactory emf = Persistence.createEntityManagerFactory("cs308");
				EntityManager em = emf.createEntityManager();
				int countingVariable = 0;
				for(String itemName : itemNames)
				{
					System.out.println("Query is " + itemName);
					try
					{
						Object obj = em.createQuery("from Games where name=:nameTemp").setParameter("nameTemp", itemName).setMaxResults(1).getSingleResult();
						Games temp = (Games) obj;
						hashmap.put(temp, Integer.parseInt(itemQuantities[countingVariable]));
					}
					catch(NoResultException e)
					{
						System.out.println("Item could not be found in the database!!!");
					}
					countingVariable++;
				}
				em.getTransaction().begin();
				Order newOrder = new Order("TODO", user);
				newOrder.setMap(hashmap);
				em.persist(newOrder);
				System.out.println("We are here!v3");
				user.addOrder(newOrder);
				em.merge(user);
				em.getTransaction().commit();
				em.close();
				emf.close();
				System.out.println("Done v2!!!!!");
				System.out.println("Done placing the order in the database v2.");
				System.out.println("All user orders are : ");
				int countTime = 1;
				for(Order o : user.getOrders())
				{
					System.out.println("-------------------------- "+ countTime + "  --------------------------");
					System.out.println(o);
					countTime++;
				}
				String mail_content= "Hello " + user.getName()+"\n";
				for (Games game: hashmap.keySet()) {
					mail_content = mail_content + "you have bought " + hashmap.get(game) + " copies of " + game.getName() + "\n";					
				}
				JavaMailUtil.sendMailwithMessage(mail_content,user.getEmail()); 
				session.removeAttribute("cart");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

}
