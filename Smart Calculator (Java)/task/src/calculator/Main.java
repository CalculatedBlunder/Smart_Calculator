package calculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigInteger;

public class Main {

    public enum Operations {
        ADDITION("+", 1),
        SUBTRACTION("-", 1),
        MULTIPLICATION("*", 2),
        DIVISION("/", 2);

        private final String symbol;
        private final int precedence;
        Operations(String symbol, int precedence) {
            this.symbol = symbol;
            this.precedence = precedence;}
        int getPrecedence() { return precedence; }
        String getSymbol() { return symbol; }

        public static Operations fromSymbol(String symbol) {
            for (Operations op : values()) {
                if (op.getSymbol().equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Invalid symbol");
        }
    }
    static Map<String,BigInteger> variables = new HashMap<>();

    static String addSpaces(String input) {
        Pattern pattern = Pattern.compile("[-+*/]+|[()]");
        Matcher matcher = pattern.matcher(input);
        StringBuilder spacedInput = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(spacedInput, " " + matcher.group() + " ");
        }
        matcher.appendTail(spacedInput);

        return spacedInput.toString().trim().replaceAll("\\s+", " ");
    }

    static String[] simplifyOperand(String[] input) {
        String[] res = new String[input.length];
        for (int i = 0; i < input.length; i++){
            String item = input[i];
            if(item.matches("\\++")){
                res[i] = "+";
            }
            else if(item.matches("\\-+")){
                long count = item.chars().filter(ch -> ch =='-').count();
                if (count % 2 == 0) {
                    res[i] = "+";
                }
                else{
                    res[i] = "-";
                }
            }
            else if(item.matches("[*/+//-][*/+//-]+")){
                throw new IllegalArgumentException("Invalid operand");
            }
            else {
                res[i] = item;
            }
        }
        return res;
    }

    static ArrayList<String> convertToPostfix(String[] items) {
        ArrayList<String> res = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String item: items) {
            if (item.matches("-?\\w+")) {
                res.add(item);
            }
            else if (item.matches("[+\\-*/]")) {
                if (stack.isEmpty() || stack.peek().equals("(")) {
                    stack.push(item);
                }
                else if (Operations.fromSymbol(item).getPrecedence() > Operations.fromSymbol(stack.peek()).getPrecedence()) {
                    stack.push(item);
                }
                else if (Operations.fromSymbol(item).getPrecedence() <= Operations.fromSymbol(stack.peek()).getPrecedence()){
                    while (!stack.isEmpty() && !stack.peek().equals("(") &&
                            Operations.fromSymbol(item).getPrecedence() <= Operations.fromSymbol(stack.peek()).getPrecedence()) {
                        res.add(stack.pop());
                    }
                    stack.push(item);
                }
            }
            else if (item.matches("\\(")) {
                stack.push("(");
            }
            else if (item.matches("\\)")) {
                while (!stack.peek().equals("(")) {
                    res.add(stack.pop());
                }
                stack.pop();
            }
        }
        while (!stack.isEmpty()) {
            res.add(stack.pop());
        }
        return res;
    }

    static void computePostfix(ArrayList<String> input){
        Stack<BigInteger> stack = new Stack<>();
        if (input.contains("(") || input.contains(")")) {
            System.out.println("Invalid expression");
            return;
        }
        for (String item: input) {
            if (item.matches("-?\\d+")) {
                stack.push(new BigInteger(item));
            }
            else if (item.matches("[A-z]+")) {
                if (variables.containsKey(item)) {
                    stack.push(variables.get(item));
                } else {
                    System.out.println("Unknown variable");
                    return;
                }
            }
            else if (item.matches("[+\\-*/]")) {
                BigInteger a = stack.pop();
                BigInteger b = stack.pop();
                switch (item) {
                    case "+" -> stack.push(a.add(b));
                    case "-" -> stack.push(b.subtract(a));
                    case "*" -> stack.push(a.multiply(b));
                    case "/" -> stack.push(b.divide(a));
                }
            }
        }
        System.out.println(stack.pop());
    }

    static void handleExpression(String input) {
        String[] operators = input.split(" ");
        if (input.isEmpty()){
            return;
        }
        String[] items = input.split(" ");
        try {
            items = simplifyOperand(items);
            ArrayList<String> postfixExpression = convertToPostfix(items);
            computePostfix(postfixExpression);
        }
        catch(IllegalArgumentException | EmptyStackException e){
            System.out.println("Invalid expression");
        }
    }

    static boolean handleCommand(String input) {
        if (input.equals("/exit")) {
            System.out.println("Bye!");
            return true; // signal loop to stop
        }
        else if (input.equals("/help")) {
            System.out.println("The program calculates the sum of numbers");
            return false; // signal loop to continue
        }
        else {
            System.out.println("Unknown command");
            return false; // signal loop to continue
        }

    }

    static void handleAssignment(String input) {
        input = input.replace(" ", "");
        String[] operators = input.split("=", 2);
        if (!operators[0].matches("[A-z]+")) {
            System.out.println("Invalid identifier");
            return;
        }

        else if (operators[1].matches("-?\\d+")) {
            variables.put(operators[0], new BigInteger(operators[1]));
            return;
        }
        else if (!operators[1].matches("[A-z]+")) {
            System.out.println("Invalid assignment");
            return;
        }
        else {
            if (variables.containsKey(operators[1])) {
                variables.put(operators[0], variables.get(operators[1]));
                return;
            }
            else {
                System.out.println("Unknown variable");
                return;
            }
        }
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // put your code here

        while (true) {
            String input = scanner.nextLine().trim();

            // handle commands
            if (input.startsWith("/")) {
                if(handleCommand(input)){
                    break;
                }
            }
            else if (input.contains("=")) {
                input = addSpaces(input);
                handleAssignment(input);
            }

            // handle expression
            else{
                input = addSpaces(input);
                handleExpression(input);
            }

        }

        scanner.close();
    }
}
