package sample.accounts.ws;

import sample.accounts.AccountNumber;
import sample.accounts.TransferException;
import sample.accounts.TransferService;
import sample.accounts.Value;
import sample.utils.ThreadSafeExecution;
import lajkonik.fp.IO;
import lajkonik.fp.Nothing;
import lajkonik.fp.Try;
import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TransferServlet extends HttpServlet {

    public static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";

    private final ThreadSafeExecution ioExecution = new ThreadSafeExecution();
    private final TransferService transferService;
    
    public TransferServlet(TransferService transferSerivice) {
        this.transferService = Objects.requireNonNull(transferSerivice);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {     
        final IO<Integer> command = parseParams(request).flatMap(p -> transferService.transfer(p.fromAccountNumber, p.toAccountNumber, p.amount));
        
        final Try<Integer> result = ioExecution.execute(command);

        writeCommand(response, result).run();  
    }
   
    private static IO<TransferCommand> parseParams(HttpServletRequest request) {
       return IO.of(() -> new TransferCommand(new AccountNumber(Long.parseLong(request.getParameter(HttpClientTransferService.FROM_ACCOUNT_PARAMETER_NAME))),
                                              new AccountNumber(Long.parseLong(request.getParameter(HttpClientTransferService.TO_ACCOUNT_PARAMETER_NAME))),
                                              new Value(Long.parseLong(request.getParameter(HttpClientTransferService.AMOUNT_PARAMETER_NAME)))));
    }
    
    private static IO<Nothing> writeCommand(HttpServletResponse response, Try<Integer> result) {
        return result.toIO(r -> IO.effect(() -> { response.setContentType(PLAIN_TEXT_CONTENT_TYPE);
                                                  response.setStatus(HttpServletResponse.SC_OK);
                                                  write(response, r.toString()); }),
                           ex -> IO.effect(() -> { response.setContentType(PLAIN_TEXT_CONTENT_TYPE);
                                                   response.setStatus(ex instanceof TransferException ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
                                                   write(response, "ERR:" + ex.getMessage());}));
    }
    
    
    private static void write(HttpServletResponse response, String str) throws IOException {
           response.getWriter().println(str);
    }
}