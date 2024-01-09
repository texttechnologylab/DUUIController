package api.duui.template;

import api.storage.DUUIMongoDBStorage;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DUUITemplateController {

    public static Document findOnePipeline(String id) {
        return DUUIMongoDBStorage
            .Pipelines()
            .find(Filters.eq(new ObjectId(id)))
            .first();
    }

    public static Document findOneComponent(String id) {
        return DUUIMongoDBStorage
            .Components()
            .find(Filters.eq(new ObjectId(id)))
            .first();
    }

    public static List<Document> findManyPipelines(int limit, int skip, Bson filter) {
        FindIterable<Document> documents = DUUIMongoDBStorage
            .Pipelines()
            .find(filter);

        if (skip != 0) documents = documents.skip(skip);
        if (limit != 0) documents = documents.limit(limit);
        return documents.into(new ArrayList<>());
    }

    public static List<Document> findManyComponents(int limit, int skip, Bson filter) {
        FindIterable<Document> documents = DUUIMongoDBStorage
            .Components()
            .find(filter);

        if (skip != 0) documents = documents.skip(skip);
        if (limit != 0) documents = documents.limit(limit);
        return documents.into(new ArrayList<>());
    }

}
