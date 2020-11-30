package dl.repositories;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import dl.models.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceRepository {

    final String TABLE_NAME = "devices";

    AmazonDynamoDB client;
    DynamoDB dynamoDB;

    public DeviceRepository() {
        client = AmazonDynamoDBClientBuilder.defaultClient();
        dynamoDB = new DynamoDB(client);
    }

    public List<Device> readAll() {

        ScanRequest scanRequest = new ScanRequest().withTableName(TABLE_NAME);
        ScanResult result = client.scan(scanRequest);

        List<Device> devices = new ArrayList<>();

        result.getItems().forEach(item -> {
            Device device = new Device();
            device.setId(item.get("id").getS());
            device.setDescription(item.get("descricao").getS());
            device.setLatitude(Double.valueOf(item.get("latitude").getN()));
            device.setLongitude(Double.valueOf(item.get("longitude").getN()));
            device.setActive(item.get("active").getBOOL());

            devices.add(device);
        });

        return devices;
    }

    public Device findById(String id) {
        final Table table = dynamoDB.getTable(TABLE_NAME);

        Item item = table.getItem("id", id);

        if (item != null) {

            return Device
                    .builder()
                    .id(item.get("id").toString())
                    .description(item.get("descricao").toString())
                    .latitude(Double.valueOf(item.get("latitude").toString()))
                    .longitude(Double.valueOf(item.get("longitude").toString()))
                    .active(Boolean.valueOf(item.get("active").toString()))
                    .build();

        }

        throw new RuntimeException(String.format("Device com id %s não encontrado", id));
    }

    public Device create(final Device device) {

        try {
            Table table = dynamoDB.getTable(TABLE_NAME);

            device.setId(UUID.randomUUID().toString());
            device.setActive(true);

            Item item = new Item()
                    .withPrimaryKey("id", device.getId())
                    .withString("descricao", device.getDescription())
                    .withDouble("latitude", device.getLatitude())
                    .withDouble("longitude", device.getLongitude())
                    .withBoolean("active", device.isActive());

            table.putItem(item);

            return device;

        } catch (Exception e) {
            System.out.println("=> Exception in create");
            e.printStackTrace();
        }

        throw new RuntimeException("Device não foi criado.");
    }

    public Device update(final Device device) {
        Table table = dynamoDB.getTable(TABLE_NAME);

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("id", device.getId())
                .withUpdateExpression("set descricao = :descricao, latitude = :latitude, longitude = :longitude, active = :active")
                .withValueMap(new ValueMap()
                        .withString(":descricao", device.getDescription())
                        .withNumber(":latitude", device.getLatitude())
                        .withNumber(":longitude", device.getLongitude())
                        .withBoolean(":active", device.isActive()))
                .withReturnValues(ReturnValue.NONE);

        table.updateItem(updateItemSpec);

        return device;
    }

    public void delete(String id) {

        Table table = dynamoDB.getTable(TABLE_NAME);
        table.deleteItem("id", id);
    }

}