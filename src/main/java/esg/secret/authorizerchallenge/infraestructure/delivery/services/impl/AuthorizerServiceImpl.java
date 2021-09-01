package esg.secret.authorizerchallenge.infraestructure.delivery.services.impl;

import esg.secret.authorizerchallenge.infraestructure.delivery.dto.AccountDTO;
import esg.secret.authorizerchallenge.infraestructure.delivery.dto.TransactionDTO;
import esg.secret.authorizerchallenge.infraestructure.delivery.responses.JsonResponse;
import esg.secret.authorizerchallenge.infraestructure.delivery.rest.OperationRest;
import esg.secret.authorizerchallenge.infraestructure.delivery.services.AuthorizerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class AuthorizerServiceImpl implements AuthorizerService {

    private final OperationServiceImpl operationService = new OperationServiceImpl();

    @Override
    public boolean run() {
        try (Scanner scan = new Scanner(System.in)) {
            while (scan.hasNext()) {
                this.parserLine(scan.nextLine());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean parserLine(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode objJson = mapper.readTree(json);
            String operation = objJson.fields().next().getKey();
            switch (operation) {
                case "account":
                    this.processAccount(objJson.get("account"));
                    break;
                case "transaction":
                    this.processTransaction(objJson.get("transaction"));
                    break;
            }
        } catch (Exception ex) {

        }
        return false;
    }

    @Override
    public boolean processAccount(JsonNode objJson) {
        boolean activeCard = false;
        int availableLimit = 0;

        if (objJson.get("active-card") != null) {
            activeCard = objJson.get("active-card").asBoolean(false);
        }

        if (objJson.get("available-limit") != null) {
            availableLimit = objJson.get("available-limit").asInt(0);
        }

        OperationRest operationRest = operationService.account(
            new AccountDTO(
                activeCard,
                availableLimit
            )
        );
        JsonResponse.stdout(operationRest);
        return operationRest.getViolations().isEmpty();
    }

    @Override
    public boolean processTransaction(JsonNode objJson) {
        String merchant = "";
        int amount = 0;
        Date datetime = null;

        if (objJson.get("merchant") != null) {
            merchant = objJson.get("merchant").asText("");
        }

        if (objJson.get("amount") != null) {
            amount = objJson.get("amount").asInt(0);
        }

        if (objJson.get("time") != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            try {
                datetime = sdf.parse(objJson.get("time").asText());
            } catch (ParseException e) {

            }
        }

        OperationRest operationRest = operationService.transaction(
            new TransactionDTO(
                merchant,
                amount,
                datetime
            )
        );
        JsonResponse.stdout(operationRest);
        return operationRest.getViolations().isEmpty();
    }
}
