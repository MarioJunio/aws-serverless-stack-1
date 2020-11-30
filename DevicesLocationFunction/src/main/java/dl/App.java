package dl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import dl.models.Device;
import dl.repositories.DeviceRepository;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    DeviceRepository deviceRepository;
    ObjectMapper objectMapper;

    {
        objectMapper = new ObjectMapper();
        deviceRepository = new DeviceRepository();
    }

    @SneakyThrows
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        final StringWriter writer = new StringWriter();

        int status = 200;

        try {
            final String httpMethod = input.getHttpMethod().toUpperCase();

            switch (httpMethod) {

                case "GET":
                    objectMapper.writeValue(writer, input.getPathParameters() != null
                            ? deviceRepository.findById(input.getPathParameters().get("id"))
                            : deviceRepository.readAll());
                    break;

                case "POST":
                    objectMapper.writeValue(writer, create(input));
                    break;

                case "PUT":
                    objectMapper.writeValue(writer, update(input));
                    break;

                case "DELETE":
                    delete(input);

            }

        } catch (RuntimeException e) {
            System.out.println("=> Exception in handleRequest");
            e.printStackTrace();
            status = 500;
            writer.append(e.getLocalizedMessage());
        } finally {
            writer.close();
        }

        return response(status, writer.toString());
    }

    Device create(final APIGatewayProxyRequestEvent input) throws IOException {

        if (input.getBody().isBlank()) {
            throw new RuntimeException("Informe as informações do device para cadastro");
        }

        return deviceRepository.create(new Device(input.getBody()));
    }

    Device update(final APIGatewayProxyRequestEvent input) throws IOException {

        if (input.getPathParameters() == null || input.getBody().isBlank()) {
            throw new RuntimeException("Informe as informações do device para atualizar");
        }

        final String id = input.getPathParameters().get("id");

        Device device = new Device(input.getBody());
        device.setId(id);

        return deviceRepository.update(device);
    }

    void delete(final APIGatewayProxyRequestEvent input) {

        if (input.getPathParameters() == null) {
            throw new RuntimeException("Informe o código do device para realizar a exclusão.");
        }

        final String id = input.getPathParameters().get("id");

        deviceRepository.delete(id);
    }

    APIGatewayProxyResponseEvent response(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        return response
                .withStatusCode(statusCode)
                .withBody(body);

    }

}
