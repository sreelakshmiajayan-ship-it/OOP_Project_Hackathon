
package service;

import dao.BillDAO;
import model.Bill;
import java.sql.SQLException;
import java.util.List;

public class BillingService {

    private final BillDAO billDAO = new BillDAO();

    /**
     * Generates and saves a bill.
     */
    public boolean generateBill(Bill bill) throws SQLException {
        if (bill == null) {
            throw new IllegalArgumentException(
                "Bill details cannot be null."
            );
        }

        bill.setPaymentStatus("Pending");
        return billDAO.addBill(bill);
    }

    /**
     * Processes payment for a bill.
     */
   
    public boolean payBill(int billId, String paymentMethod)
        throws SQLException {

    if (billId <= 0) {
        throw new IllegalArgumentException("Invalid bill ID.");
    }

    if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Payment method cannot be empty."
        );
    }

    Bill bill = billDAO.getBillById(billId);

    if (bill == null) {
        throw new IllegalArgumentException(
            "Bill with ID " + billId + " not found."
        );
    }

    if ("Paid".equalsIgnoreCase(bill.getPaymentStatus())) {
        throw new IllegalArgumentException(
            "This bill has already been paid."
        );
    }

    return billDAO.markBillAsPaid(billId, paymentMethod);
}


    /**
     * Retrieves all bills from the database.
     */
    public List<Bill> getAllBills() throws SQLException {
        return billDAO.getAllBills();
 
    }


    
    /**
     * Retrieves bills belonging to a specific patient.
     */
    public List<Bill> getBillsByPatientId(int patientId)
            throws SQLException {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID."
            );
        }

        return billDAO.getBillsByPatientId(patientId);
    }

}
