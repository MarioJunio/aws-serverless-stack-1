package dl.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "devices")
public class Device implements Serializable {

    public Device(String json) throws IOException {
        Device device = new ObjectMapper().readValue(json, Device.class);

        setId(device.getId());
        setDescription(device.getDescription());
        setLatitude(device.getLatitude());
        setLongitude(device.getLongitude());
        setActive(device.isActive());
    }

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "descricao")
    private String description;

    @DynamoDBAttribute(attributeName = "latitude")
    private double latitude;

    @DynamoDBAttribute(attributeName = "longitude")
    private double longitude;

    @DynamoDBAttribute(attributeName = "active")
    private boolean active;

}
