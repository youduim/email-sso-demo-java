package im.youdu.demo.exmail;

import im.youdu.sdk.client.IdentifyClient;
import im.youdu.sdk.entity.UserInfo;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SSOServlet extends HttpServlet {
    private final static Logger log = Logger.getLogger(SSOServlet.class.getName());

    private String host;
    private String initErrMsg;
    private IdentifyClient ydIdentifyClient;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errMsg = "";
        for(;;){
            if(null == ydIdentifyClient){
                errMsg = "有度服务信息未初始化:"+initErrMsg;
                break;
            }

            String ydtoken = req.getParameter("ydtoken");
            if(null != ydtoken){
                ydtoken = ydtoken.trim();
            }else{
                ydtoken = "";
            }
            if(ydtoken.length()==0){
                errMsg = "没有接收到有度身份认证token";
                break;
            }
            log.info("读到有度token:"+ydtoken);
            try {
                UserInfo user = ydIdentifyClient.idetify(ydtoken);
                String email = user.getEmail();
                if(null == email || email.length() == 0){
                    errMsg = "读到邮箱地址为空";
                   break;
                }
                log.info("读到邮箱地址:"+email);
                String url = this.getSSOUrlByEmail(email);
                resp.sendRedirect(url);
                return;
            } catch (Exception e) {
                errMsg = "有度身份认证异常:"+e.getMessage();
            }

            break;
        }
        log.error(errMsg);
        req.setAttribute("errMsg", errMsg);
        req.getRequestDispatcher("error.jsp").forward(req,resp);
    }

    @Override
    public void init() throws ServletException {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = this.getClass().getResourceAsStream("/ydapp.properties");
            prop.load(in);
            host = prop.getProperty("host");
            log.info("读取到有度服务地址:"+host);
            ydIdentifyClient = new IdentifyClient(host);
        } catch (Exception e) {
            initErrMsg = "读取有度服务配置发生错误: "+e.getMessage();
            log.error(initErrMsg);
        }
    }

    //TODO 这里只是一段示例代码, 具体实现请参照您企业邮箱服务商的接口规范
    private String getSSOUrlByEmail(String email){
        log.info("获取企业邮单点登录地址:"+email);
        return "https://youdu.im";
    }

}
