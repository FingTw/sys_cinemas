import net.sf.jasperreports.engine.JasperCompileManager;
public class CompileJasper {
    public static void main(String[] args) {
        try {
            JasperCompileManager.compileReport("d:/sys_cinemas/cinema-microservices/cinema-admin/src/main/resources/reports/receipt.jrxml");
            System.out.println("Compile Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
