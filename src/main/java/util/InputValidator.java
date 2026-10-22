
package util;

public class InputValidator {

    private InputValidator() {
    }

    // Check whether a field is empty
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Validate email address
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email)
                && email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                );
    }

    // Validate 10-digit phone number
    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone)
                && phone.matches("[0-9]{10}");
    }

    // Validate positive integer values and IDs
    public static boolean isValidId(String id) {
        if (!isNotEmpty(id)) {
            return false;
        }

        try {
            return Integer.parseInt(id.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate age
    public static boolean isValidAge(String age) {
        if (!isNotEmpty(age)) {
            return false;
        }

        try {
            int value = Integer.parseInt(age.trim());
            return value > 0 && value <= 120;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate password
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    // Validate non-negative amount
    public static boolean isValidAmount(String amount) {
        if (!isNotEmpty(amount)) {
            return false;
        }

        try {
            double value = Double.parseDouble(amount.trim());
            return Double.isFinite(value) && value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate a positive quantity
    public static boolean isValidQuantity(String quantity) {
        if (!isNotEmpty(quantity)) {
            return false;
        }

        try {
            return Integer.parseInt(quantity.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
