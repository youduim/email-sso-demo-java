package exmail.example.servlet;

import im.youdu.sdk.client.IdentifyClient;
import im.youdu.sdk.entity.UserInfo;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SSOServlet extends HttpServlet {
    private final static Logger log = Logger.getLogger(SSOServlet.class.getName());

    private String host = "127.0.0.1:7080";
    private IdentifyClient ydIdentifyClient = new IdentifyClient(host);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errMsg = "";
        String ydToken = req.getParameter("ydtoken");
        log.info("[exmail][sso] read token:"+ydToken);
        for(;;){
            if(null == ydToken || ydToken.trim().length()==0){
                errMsg = "服务没有接收到有度身份认证token";
                break;
            }
            try {
                UserInfo user = ydIdentifyClient.idetify(ydToken);
                String email = user.getEmail();
                if(null == email || email.length() == 0){
                    log.warn(String.format("[exmail][sso] get user from yd ok, but found email is blank: %s",user.getUserId()));
                    errMsg = "你的邮箱地址为空";
                   break;
                }
                log.info("[exmail][sso] read email:"+email);
                String url = this.getSSOUrlByEmail(email);
                resp.sendRedirect(url);
                return;
            } catch (Exception e) {
                log.error("[exmail][sso] get user from yd exception: "+e.getMessage());
                errMsg = "有度身份认证异常:"+e.getMessage();
               break;
            }
        }
        req.setAttribute("errMsg", errMsg);
        req.getRequestDispatcher("error.jsp").forward(req,resp);
    }

    private String getSSOUrlByEmail(String email){
        //TODO 获取用户的企业邮单点登录URL
        log.info("[exmail][sso] get login url by email:"+email);
        return "http://youdu.im";
    }

}
